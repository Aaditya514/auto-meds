package com.automeds.service;

import com.automeds.dto.AdminDashboardDTO;
import com.automeds.dto.MedicineDTO;
import com.automeds.dto.OrderDTO;
import com.automeds.dto.SubscriptionResponseDTO;
import com.automeds.entity.Medicine;
import com.automeds.entity.Order;
import com.automeds.entity.Subscription;
import com.automeds.entity.User;
import com.automeds.exception.BadRequestException;
import com.automeds.exception.ResourceNotFoundException;
import com.automeds.repository.MedicineRepository;
import com.automeds.repository.OrderRepository;
import com.automeds.repository.SubscriptionRepository;
import com.automeds.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class AdminServiceTest {

    private UserRepository userRepository;
    private MedicineRepository medicineRepository;
    private SubscriptionRepository subscriptionRepository;
    private OrderRepository orderRepository;
    private MedicineService medicineService;
    private SubscriptionService subscriptionService;
    private OrderService orderService;
    private NotificationService notificationService;
    private EmailService emailService;
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        medicineRepository = Mockito.mock(MedicineRepository.class);
        subscriptionRepository = Mockito.mock(SubscriptionRepository.class);
        orderRepository = Mockito.mock(OrderRepository.class);
        medicineService = Mockito.mock(MedicineService.class);
        subscriptionService = Mockito.mock(SubscriptionService.class);
        orderService = Mockito.mock(OrderService.class);
        notificationService = Mockito.mock(NotificationService.class);
        emailService = Mockito.mock(EmailService.class);

        adminService = new AdminService(userRepository, medicineRepository, subscriptionRepository, orderRepository, medicineService, subscriptionService, orderService, notificationService, emailService);
    }

    @Test
    void testGetDashboardMetrics() {
        Mockito.when(userRepository.findByRole("PATIENT")).thenReturn(Collections.singletonList(new User()));
        Mockito.when(medicineRepository.count()).thenReturn(10L);
        Mockito.when(subscriptionRepository.countByStatus("ACTIVE")).thenReturn(5L);
        Mockito.when(subscriptionRepository.countByStatus("PENDING")).thenReturn(2L);
        Mockito.when(subscriptionRepository.findByStatusAndNextRefillDateLessThanEqual(any(), any())).thenReturn(Collections.emptyList());
        Mockito.when(orderRepository.countByOrderStatus("PENDING")).thenReturn(3L);
        Mockito.when(medicineRepository.findByActiveAndStockQuantityLessThanEqual(1, 10)).thenReturn(Collections.emptyList());
        Mockito.when(medicineRepository.findByActiveAndStockQuantityEquals(1, 0)).thenReturn(Collections.emptyList());

        AdminDashboardDTO metrics = adminService.getDashboardMetrics();
        assertNotNull(metrics);
        assertEquals(1L, metrics.getTotalPatients());
        assertEquals(10L, metrics.getTotalMedicines());
    }

    @Test
    void testGetPendingSubscriptionRequests() {
        Subscription sub = new Subscription();
        Mockito.when(subscriptionRepository.findByStatus("PENDING")).thenReturn(Collections.singletonList(sub));
        List<SubscriptionResponseDTO> res = adminService.getPendingSubscriptionRequests();
        assertNotNull(res);
    }

    @Test
    void testApproveSubscription() {
        User patient = new User();
        patient.setId(1L);

        Medicine m = new Medicine();
        m.setId(2L);
        m.setMedicineName("Med");

        Subscription sub = new Subscription();
        sub.setId(10L);
        sub.setPatient(patient);
        sub.setQuantity(30);
        sub.setDosage("1 tablet");

        Mockito.when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(sub));
        Mockito.when(medicineRepository.findById(2L)).thenReturn(Optional.of(m));
        Mockito.when(subscriptionService.calculateDurationDays(30, "1 tablet")).thenReturn(30);
        Mockito.when(subscriptionRepository.save(any())).thenReturn(sub);
        SubscriptionResponseDTO dto = new SubscriptionResponseDTO();
        dto.setMedicineName("Med");
        Mockito.when(subscriptionService.convertToDTO(any())).thenReturn(dto);

        adminService.approveSubscription(10L, Collections.singletonList(new com.automeds.dto.MedicineAssignmentDTO(2L, "1 tablet", "Once Daily", 30)));
        assertEquals("ACTIVE", sub.getStatus());
        assertEquals(m, sub.getMedicine());
    }

    @Test
    void testRejectSubscription() {
        User patient = new User();
        patient.setId(1L);
        Medicine m = new Medicine();
        m.setMedicineName("Med");

        Subscription sub = new Subscription();
        sub.setId(10L);
        sub.setPatient(patient);
        sub.setMedicine(m);

        Mockito.when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(sub));
        Mockito.when(subscriptionRepository.save(sub)).thenReturn(sub);

        adminService.rejectSubscription(10L, "Invalid image");
        assertEquals("REJECTED", sub.getStatus());
    }

    @Test
    void testRequestClarification() {
        User patient = new User();
        patient.setId(1L);

        Subscription sub = new Subscription();
        sub.setId(10L);
        sub.setPatient(patient);
        sub.setMedicine(null); // Pending subscription requests have null medicine initially

        Mockito.when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(sub));
        Mockito.when(subscriptionRepository.save(sub)).thenReturn(sub);

        adminService.requestClarification(10L, "Re-upload prescription");
        assertEquals("CLARIFICATION_REQUIRED", sub.getStatus());
    }

    @Test
    void testCreateAndUpdateMedicine() {
        MedicineDTO dto = new MedicineDTO();
        dto.setMedicineName("Med");
        dto.setPrice(BigDecimal.valueOf(10.0));
        dto.setStockQuantity(50);
        dto.setRequiresPrescription(true);

        Medicine saved = new Medicine();
        saved.setId(1L);
        Mockito.when(medicineRepository.save(any())).thenReturn(saved);

        adminService.createMedicine(dto);
        Mockito.verify(medicineRepository).save(any());

        Mockito.when(medicineRepository.findById(1L)).thenReturn(Optional.of(saved));
        dto.setExpiryDate(LocalDateTime.now());
        adminService.updateMedicine(1L, dto);
        Mockito.verify(medicineRepository, Mockito.times(2)).save(any());
    }

    @Test
    void testDeactivateMedicine() {
        Medicine m = new Medicine();
        m.setId(1L);
        m.setActive(1);

        Mockito.when(medicineRepository.findById(1L)).thenReturn(Optional.of(m));
        Mockito.when(medicineRepository.save(m)).thenReturn(m);

        adminService.deactivateMedicine(1L);
        assertEquals(0, m.getActive());
    }

    @Test
    void testUpdateStockSuccessAndNegative() {
        Medicine m = new Medicine();
        m.setId(1L);
        m.setStockQuantity(10);

        Mockito.when(medicineRepository.findById(1L)).thenReturn(Optional.of(m));
        Mockito.when(medicineRepository.save(m)).thenReturn(m);

        adminService.updateStock(1L, 20);
        assertEquals(20, m.getStockQuantity());

        assertThrows(BadRequestException.class, () -> adminService.updateStock(1L, -5));
    }

    @Test
    void testGetAllOrdersAndUpdateOrderStatus() {
        User u = new User();
        u.setId(1L);

        Order o = new Order();
        o.setId(100L);
        o.setPatient(u);

        Mockito.when(orderRepository.findAllByOrderByOrderDateDesc()).thenReturn(Collections.singletonList(o));
        List<OrderDTO> orders = adminService.getAllOrders();
        assertNotNull(orders);

        Mockito.when(orderRepository.findById(100L)).thenReturn(Optional.of(o));
        Mockito.when(orderRepository.save(o)).thenReturn(o);

        adminService.updateOrderStatus(100L, "DELIVERED");
        assertEquals("DELIVERED", o.getOrderStatus());
        assertEquals("PAID", o.getPaymentStatus());

        Mockito.when(userRepository.findByRole("PATIENT")).thenReturn(Collections.singletonList(u));
        List<User> patients = adminService.getAllPatients();
        assertEquals(1, patients.size());
    }
}


