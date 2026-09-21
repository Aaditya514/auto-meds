package com.automeds.scheduler;

import com.automeds.entity.*;
import com.automeds.repository.MedicineRepository;
import com.automeds.repository.SubscriptionRepository;
import com.automeds.scheduler.AutoRefillScheduler;
import com.automeds.service.NotificationService;
import com.automeds.service.OrderService;
import com.automeds.service.SubscriptionService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;

class AutoRefillSchedulerTest {

    private SubscriptionRepository subscriptionRepository;
    private MedicineRepository medicineRepository;
    private OrderService orderService;
    private SubscriptionService subscriptionService;
    private NotificationService notificationService;
    private AutoRefillScheduler scheduler;

    @BeforeEach
    void setUp() {
        subscriptionRepository = Mockito.mock(SubscriptionRepository.class);
        medicineRepository = Mockito.mock(MedicineRepository.class);
        orderService = Mockito.mock(OrderService.class);
        subscriptionService = Mockito.mock(SubscriptionService.class);
        notificationService = Mockito.mock(NotificationService.class);

        scheduler = new AutoRefillScheduler(subscriptionRepository, medicineRepository, orderService, subscriptionService, notificationService);
        ReflectionTestUtils.setField(scheduler, "refillBufferDays", 5);
    }

    @Test
    void testProcessAutoRefillsExpiredPrescription() {
        User u = new User();
        u.setId(1L);

        Medicine m = new Medicine();
        m.setId(2L);
        m.setMedicineName("Med");

        Prescription p = new Prescription();
        p.setExpiryDate(LocalDateTime.now().minusDays(1));

        Subscription sub = new Subscription();
        sub.setId(10L);
        sub.setPatient(u);
        sub.setMedicine(m);
        sub.setPrescription(p);
        sub.setStatus("ACTIVE");

        Mockito.when(subscriptionRepository.findByStatusAndNextRefillDateLessThanEqual(any(), any()))
                .thenReturn(Collections.singletonList(sub));

        scheduler.processAutoRefills();

        Mockito.verify(subscriptionRepository).save(sub);
    }

    @Test
    void testProcessAutoRefillsSuccessOrderGeneration() {
        User u = new User();
        u.setId(1L);

        Medicine m = new Medicine();
        m.setId(2L);
        m.setActive(1);
        m.setStockQuantity(50);
        m.setMedicineName("Med");

        Prescription p = new Prescription();
        p.setExpiryDate(LocalDateTime.now().plusDays(30));

        Subscription sub = new Subscription();
        sub.setId(10L);
        sub.setPatient(u);
        sub.setMedicine(m);
        sub.setPrescription(p);
        sub.setQuantity(5);
        sub.setDosage("1 tab");

        Mockito.when(subscriptionRepository.findByStatusAndNextRefillDateLessThanEqual(any(), any()))
                .thenReturn(Collections.singletonList(sub));
        Mockito.when(subscriptionService.calculateDurationDays(5, "1 tab")).thenReturn(30);

        Order order = new Order();
        order.setId(100L);
        Mockito.when(orderService.createSubscriptionRefillOrder(sub, m)).thenReturn(order);

        scheduler.processAutoRefills();

        Mockito.verify(orderService).createSubscriptionRefillOrder(sub, m);
        Mockito.verify(subscriptionRepository).save(sub);
    }

    @Test
    void testProcessAutoRefillsOutOfStockWithAlternatives() {
        User u = new User();
        u.setId(1L);

        Medicine m = new Medicine();
        m.setId(2L);
        m.setActive(1);
        m.setStockQuantity(0);
        m.setMedicineName("Med");
        m.setComposition("Comp");
        m.setStrength("10mg");

        Medicine alt = new Medicine();
        alt.setId(3L);
        alt.setStockQuantity(20);
        alt.setBrandName("AltBrand");
        alt.setMedicineName("AltMed");
        alt.setPrice(java.math.BigDecimal.TEN);

        Subscription sub = new Subscription();
        sub.setId(10L);
        sub.setPatient(u);
        sub.setMedicine(m);
        sub.setQuantity(5);

        Mockito.when(subscriptionRepository.findByStatusAndNextRefillDateLessThanEqual(any(), any()))
                .thenReturn(Collections.singletonList(sub));
        Mockito.when(medicineRepository.findAlternatives("Comp", "10mg", 2L))
                .thenReturn(Collections.singletonList(alt));

        scheduler.processAutoRefills();

        Mockito.verify(notificationService).createNotification(any(), any(), Mockito.contains("out of stock"), any());
    }

    @Test
    void testProcessAutoRefillsOutOfStockNoAlternatives() {
        User u = new User();
        u.setId(1L);

        Medicine m = new Medicine();
        m.setId(2L);
        m.setActive(1);
        m.setStockQuantity(0);
        m.setMedicineName("Med");
        m.setComposition("Comp");
        m.setStrength("10mg");

        Subscription sub = new Subscription();
        sub.setId(10L);
        sub.setPatient(u);
        sub.setMedicine(m);
        sub.setQuantity(5);

        Mockito.when(subscriptionRepository.findByStatusAndNextRefillDateLessThanEqual(any(), any()))
                .thenReturn(Collections.singletonList(sub));
        Mockito.when(medicineRepository.findAlternatives("Comp", "10mg", 2L))
                .thenReturn(Collections.emptyList());

        scheduler.processAutoRefills();

        Mockito.verify(notificationService).createNotification(any(), any(), Mockito.contains("No exact composition"), any());
    }
}


