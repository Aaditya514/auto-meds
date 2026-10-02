package com.automeds.service;

import com.automeds.dto.AddToCartRequest;
import com.automeds.dto.CheckoutRequest;
import com.automeds.dto.SubscriptionResponseDTO;
import com.automeds.entity.Cart;
import com.automeds.entity.CartItem;
import com.automeds.entity.Medicine;
import com.automeds.entity.Subscription;
import com.automeds.entity.User;
import com.automeds.exception.InsufficientStockException;
import com.automeds.repository.CartItemRepository;
import com.automeds.repository.CartRepository;
import com.automeds.repository.MedicineRepository;
import com.automeds.repository.OrderRepository;
import com.automeds.repository.SubscriptionRepository;
import com.automeds.repository.UserRepository;
import com.automeds.scheduler.AutoRefillScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventorySoftLockTest {

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private EmailService emailService;

    @Mock
    private PrescriptionService prescriptionService;

    private CartService cartService;
    private OrderService orderService;
    private SubscriptionService subscriptionService;
    private AutoRefillScheduler autoRefillScheduler;

    private User patient;
    private Medicine medicine;
    private Subscription subscription;

    @BeforeEach
    void setUp() {
        cartService = new CartService(cartRepository, cartItemRepository, medicineRepository, userRepository);
        orderService = new OrderService(orderRepository, cartService, userRepository, medicineRepository, notificationService, emailService);
        subscriptionService = new SubscriptionService(subscriptionRepository, medicineRepository, userRepository, prescriptionService, notificationService, orderRepository);
        autoRefillScheduler = new AutoRefillScheduler(subscriptionRepository, medicineRepository, orderService, subscriptionService, notificationService);

        patient = new User();
        patient.setId(1L);
        patient.setName("Aaditya Aanand");
        patient.setEmail("aaditya@example.com");

        medicine = new Medicine();
        medicine.setId(10L);
        medicine.setMedicineName("Metformin 500mg");
        medicine.setBrandName("Glycomet");
        medicine.setStockQuantity(50);
        medicine.setReservedQuantity(30);
        medicine.setPrice(new BigDecimal("120.00"));
        medicine.setActive(1);

        subscription = new Subscription();
        subscription.setId(100L);
        subscription.setPatient(patient);
        subscription.setMedicine(medicine);
        subscription.setQuantity(30);
        subscription.setStatus("ACTIVE");
        subscription.setReservationStatus("NONE");
        subscription.setNextRefillDate(LocalDateTime.now().plusDays(4));
    }

    @Test
    @DisplayName("Medicine available quantity computes physical stock minus reserved stock")
    void testAvailableQuantityComputation() {
        assertEquals(50, medicine.getStockQuantity());
        assertEquals(30, medicine.getReservedQuantity());
        assertEquals(20, medicine.getAvailableQuantity());

        // Zero out physical stock
        medicine.setStockQuantity(10);
        medicine.setReservedQuantity(15);
        assertEquals(0, medicine.getAvailableQuantity());
    }

    @Test
    @DisplayName("CartService blocks ad-hoc purchases exceeding available quantity")
    void testCartBlocksExceedingAvailableStock() {
        // Stock: 10, Reserved: 8 -> Available: 2. Requesting 5 (<= MAX_QUANTITY_PER_ITEM of 10)
        medicine.setStockQuantity(10);
        medicine.setReservedQuantity(8);

        AddToCartRequest request = new AddToCartRequest(10L, 5);

        Cart cart = new Cart();
        cart.setId(5L);
        cart.setPatient(patient);

        when(medicineRepository.findById(10L)).thenReturn(Optional.of(medicine));
        when(cartRepository.findByPatientId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndMedicineId(5L, 10L)).thenReturn(Optional.empty());

        InsufficientStockException ex = assertThrows(InsufficientStockException.class, () ->
                cartService.addItemToCart(patient.getId(), request)
        );

        assertTrue(ex.getMessage().contains("Only 2 units available for purchase"));
        assertTrue(ex.getMessage().contains("8 units are reserved for ongoing patient subscriptions"));
    }

    @Test
    @DisplayName("OrderService checkout uses atomic decrement and rejects when available stock depleted")
    void testCheckoutAtomicDecrementRejection() {
        Cart cart = new Cart();
        cart.setId(5L);
        cart.setPatient(patient);

        CartItem item = new CartItem();
        item.setId(20L);
        item.setCart(cart);
        item.setMedicine(medicine);
        item.setQuantity(10);
        item.setPriceAtAddition(medicine.getPrice());

        List<CartItem> items = new ArrayList<>();
        items.add(item);
        cart.setItems(items);

        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(cartRepository.findByPatientId(1L)).thenReturn(Optional.of(cart));
        // Atomic decrement fails (0 rows updated)
        when(medicineRepository.deductAvailableStock(10L, 10)).thenReturn(0);

        CheckoutRequest checkoutRequest = new CheckoutRequest("123 Street", "CASH_ON_DELIVERY");

        InsufficientStockException ex = assertThrows(InsufficientStockException.class, () ->
                orderService.checkoutCart(patient.getId(), checkoutRequest)
        );

        assertTrue(ex.getMessage().contains("Insufficient stock available"));
    }

    @Test
    @DisplayName("AutoRefillScheduler successfully soft-locks 5 days prior to refill")
    void testScheduler5DaySoftLockSuccess() {
        when(medicineRepository.reserveStock(10L, 30)).thenReturn(1);

        autoRefillScheduler.reserveInventoryForSubscription(subscription);

        assertEquals("RESERVED", subscription.getReservationStatus());
        assertNotNull(subscription.getReservationDate());
        verify(subscriptionRepository).save(subscription);
        verify(notificationService).createNotification(eq(1L), contains("Refill Secured"), anyString(), eq("SUCCESS"));
    }

    @Test
    @DisplayName("AutoRefillScheduler flags deficit when available stock is insufficient")
    void testScheduler5DaySoftLockDeficit() {
        when(medicineRepository.reserveStock(10L, 30)).thenReturn(0);

        autoRefillScheduler.reserveInventoryForSubscription(subscription);

        assertEquals("OUT_OF_STOCK_DEFICIT", subscription.getReservationStatus());
        verify(subscriptionRepository).save(subscription);
        verify(notificationService).createNotification(eq(1L), contains("Refill Notice"), anyString(), eq("WARNING"));
    }

    @Test
    @DisplayName("Cancelling a reserved subscription releases reserved inventory back to pool")
    void testCancelReservedSubscriptionReleasesStock() {
        subscription.setReservationStatus("RESERVED");

        when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(subscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));

        SubscriptionResponseDTO response = subscriptionService.cancelSubscription(1L, 100L);

        assertEquals("CANCELLED", response.getStatus());
        assertEquals("NONE", response.getReservationStatus());
        verify(medicineRepository).releaseReservedStock(10L, 30);
    }

    @Test
    @DisplayName("Pausing a reserved subscription releases reserved inventory back to pool")
    void testPauseReservedSubscriptionReleasesStock() {
        subscription.setStatus("ACTIVE");
        subscription.setReservationStatus("RESERVED");

        when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(subscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));

        SubscriptionResponseDTO response = subscriptionService.pauseSubscription(1L, 100L);

        assertEquals("PAUSED", response.getStatus());
        assertEquals("NONE", response.getReservationStatus());
        verify(medicineRepository).releaseReservedStock(10L, 30);
    }
}
