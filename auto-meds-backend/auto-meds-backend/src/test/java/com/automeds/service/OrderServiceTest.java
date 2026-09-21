package com.automeds.service;

import com.automeds.dto.CheckoutRequest;
import com.automeds.dto.OrderDTO;
import com.automeds.entity.*;
import com.automeds.exception.BadRequestException;
import com.automeds.exception.InsufficientStockException;
import com.automeds.exception.ResourceNotFoundException;
import com.automeds.repository.MedicineRepository;
import com.automeds.repository.OrderRepository;
import com.automeds.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class OrderServiceTest {

    private OrderRepository orderRepository;
    private CartService cartService;
    private UserRepository userRepository;
    private MedicineRepository medicineRepository;
    private NotificationService notificationService;
    private EmailService emailService;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = Mockito.mock(OrderRepository.class);
        cartService = Mockito.mock(CartService.class);
        userRepository = Mockito.mock(UserRepository.class);
        medicineRepository = Mockito.mock(MedicineRepository.class);
        notificationService = Mockito.mock(NotificationService.class);
        emailService = Mockito.mock(EmailService.class);

        orderService = new OrderService(orderRepository, cartService, userRepository, medicineRepository, notificationService, emailService);
    }

    @Test
    void testCheckoutCartSuccess() {
        User u = new User();
        u.setId(1L);
        u.setName("John");

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setPatient(u);

        Medicine m = new Medicine();
        m.setId(2L);
        m.setActive(1);
        m.setStockQuantity(50);
        m.setPrice(BigDecimal.valueOf(20.0));
        m.setMedicineName("Med");

        CartItem item = new CartItem();
        item.setId(100L);
        item.setCart(cart);
        item.setMedicine(m);
        item.setQuantity(2);

        cart.setItems(Collections.singletonList(item));

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        Mockito.when(cartService.getOrCreateCartForPatient(1L)).thenReturn(cart);

        Order saved = new Order();
        saved.setId(500L);
        saved.setPatient(u);
        saved.setItems(new ArrayList<>());
        saved.setTotalAmount(BigDecimal.valueOf(80.0));
        saved.setExpectedDeliveryDate(java.time.LocalDateTime.now().plusDays(3));

        Mockito.when(orderRepository.save(any(Order.class))).thenReturn(saved);

        CheckoutRequest req = new CheckoutRequest("Address 123", "COD");
        OrderDTO res = orderService.checkoutCart(1L, req);

        assertNotNull(res);
        assertEquals(500L, res.getId());
        Mockito.verify(cartService).clearCart(1L);
    }

    @Test
    void testCheckoutCartDefaultPaymentMethod() {
        User u = new User();
        u.setId(1L);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setPatient(u);

        Medicine m = new Medicine();
        m.setId(2L);
        m.setActive(1);
        m.setStockQuantity(50);
        m.setPrice(BigDecimal.valueOf(20.0));

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setMedicine(m);
        item.setQuantity(1);

        cart.setItems(Collections.singletonList(item));

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        Mockito.when(cartService.getOrCreateCartForPatient(1L)).thenReturn(cart);

        Order saved = new Order();
        saved.setId(501L);
        saved.setPatient(u);
        saved.setItems(new ArrayList<>());
        saved.setExpectedDeliveryDate(java.time.LocalDateTime.now().plusDays(3));

        Mockito.when(orderRepository.save(any(Order.class))).thenReturn(saved);

        CheckoutRequest req = new CheckoutRequest("Address 123", null);
        OrderDTO res = orderService.checkoutCart(1L, req);

        assertNotNull(res);
    }

    @Test
    void testCheckoutCartEmptyCart() {
        User u = new User();
        u.setId(1L);
        Cart cart = new Cart();
        cart.setItems(Collections.emptyList());

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        Mockito.when(cartService.getOrCreateCartForPatient(1L)).thenReturn(cart);

        CheckoutRequest req = new CheckoutRequest("Address", "COD");
        assertThrows(BadRequestException.class, () -> orderService.checkoutCart(1L, req));
    }

    @Test
    void testCheckoutCartUserNotFound() {
        Mockito.when(userRepository.findById(99L)).thenReturn(Optional.empty());
        CheckoutRequest req = new CheckoutRequest("Address", "COD");
        assertThrows(ResourceNotFoundException.class, () -> orderService.checkoutCart(99L, req));
    }

    @Test
    void testCheckoutCartInactiveMedicine() {
        User u = new User();
        u.setId(1L);

        Cart cart = new Cart();
        cart.setPatient(u);

        Medicine m = new Medicine();
        m.setId(2L);
        m.setActive(0);

        CartItem item = new CartItem();
        item.setMedicine(m);
        item.setQuantity(1);

        cart.setItems(Collections.singletonList(item));

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        Mockito.when(cartService.getOrCreateCartForPatient(1L)).thenReturn(cart);

        CheckoutRequest req = new CheckoutRequest("Address", "COD");
        assertThrows(BadRequestException.class, () -> orderService.checkoutCart(1L, req));
    }

    @Test
    void testCheckoutCartInsufficientStock() {
        User u = new User();
        u.setId(1L);

        Cart cart = new Cart();
        cart.setPatient(u);

        Medicine m = new Medicine();
        m.setId(2L);
        m.setActive(1);
        m.setStockQuantity(1);

        CartItem item = new CartItem();
        item.setMedicine(m);
        item.setQuantity(5);

        cart.setItems(Collections.singletonList(item));

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        Mockito.when(cartService.getOrCreateCartForPatient(1L)).thenReturn(cart);

        CheckoutRequest req = new CheckoutRequest("Address", "COD");
        assertThrows(InsufficientStockException.class, () -> orderService.checkoutCart(1L, req));
    }

    @Test
    void testCreateSubscriptionRefillOrder() {
        User u = new User();
        u.setId(1L);
        u.setAddress("Street 1");
        u.setCity("City");

        Subscription sub = new Subscription();
        sub.setId(10L);
        sub.setPatient(u);
        sub.setQuantity(30);

        Medicine m = new Medicine();
        m.setId(2L);
        m.setStockQuantity(100);
        m.setPrice(BigDecimal.valueOf(10.0));

        Order saved = new Order();
        saved.setId(200L);

        Mockito.when(orderRepository.save(any(Order.class))).thenReturn(saved);

        Order res = orderService.createSubscriptionRefillOrder(sub, m);
        assertNotNull(res);
        assertEquals(70, m.getStockQuantity());
    }

    @Test
    void testCreateSubscriptionRefillOrderNullAddress() {
        User u = new User();
        u.setId(1L);
        u.setAddress(null);

        Subscription sub = new Subscription();
        sub.setId(10L);
        sub.setPatient(u);
        sub.setQuantity(5);

        Medicine m = new Medicine();
        m.setId(2L);
        m.setStockQuantity(100);
        m.setPrice(BigDecimal.valueOf(10.0));

        Order saved = new Order();
        saved.setId(201L);

        Mockito.when(orderRepository.save(any(Order.class))).thenReturn(saved);

        Order res = orderService.createSubscriptionRefillOrder(sub, m);
        assertNotNull(res);
    }

    @Test
    void testGetOrdersForPatient() {
        User u = new User();
        u.setId(1L);

        Order o = new Order();
        o.setId(100L);
        o.setPatient(u);

        Mockito.when(orderRepository.findByPatientIdOrderByOrderDateDesc(1L)).thenReturn(Collections.singletonList(o));

        List<OrderDTO> orders = orderService.getOrdersForPatient(1L);
        assertEquals(1, orders.size());
    }

    @Test
    void testGetOrderById() {
        User u = new User();
        u.setId(1L);

        Order o = new Order();
        o.setId(100L);
        o.setPatient(u);

        Mockito.when(orderRepository.findById(100L)).thenReturn(Optional.of(o));

        OrderDTO res = orderService.getOrderById(100L);
        assertNotNull(res);
        assertEquals(100L, res.getId());
    }

    @Test
    void testGetOrderByIdNotFound() {
        Mockito.when(orderRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(99L));
    }
}


