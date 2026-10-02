package com.automeds.service;

import com.automeds.dto.DeficitSubscriptionDTO;
import com.automeds.dto.ProcurementAlertDTO;
import com.automeds.dto.RestockRequestDTO;
import com.automeds.dto.RestockResponseDTO;
import com.automeds.entity.Medicine;
import com.automeds.entity.Prescription;
import com.automeds.entity.Subscription;
import com.automeds.entity.User;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcurementAndNotificationTest {

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private EmailService emailService;

    @Mock
    private MedicineService medicineService;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private OrderService orderService;

    private AdminService adminService;
    private AutoRefillScheduler autoRefillScheduler;

    private User patient;
    private Medicine medicine;
    private Subscription subscription;
    private Prescription prescription;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(
                userRepository,
                medicineRepository,
                subscriptionRepository,
                orderRepository,
                medicineService,
                subscriptionService,
                orderService,
                notificationService,
                emailService
        );

        autoRefillScheduler = new AutoRefillScheduler(
                subscriptionRepository,
                medicineRepository,
                orderService,
                subscriptionService,
                notificationService
        );

        patient = new User(1L, "Rajesh Kumar", "rajesh@example.com", "pass", "PATIENT", "9876543210", "123 Street", "City", "State", "123456");

        medicine = new Medicine();
        medicine.setId(10L);
        medicine.setMedicineName("Atorvastatin 20mg");
        medicine.setBrandName("Lipitor");
        medicine.setComposition("Atorvastatin");
        medicine.setStrength("20mg");
        medicine.setPrice(new BigDecimal("150.00"));
        medicine.setStockQuantity(20);
        medicine.setReservedQuantity(5);
        medicine.setReorderThreshold(25);
        medicine.setSuggestedReorderPackSize(100);
        medicine.setActive(1);

        prescription = new Prescription();
        prescription.setId(50L);
        prescription.setPatient(patient);
        prescription.setExpiryDate(LocalDateTime.now().plusDays(17));

        subscription = new Subscription();
        subscription.setId(100L);
        subscription.setPatient(patient);
        subscription.setMedicine(medicine);
        subscription.setPrescription(prescription);
        subscription.setQuantity(30);
        subscription.setDosage("1 tablet daily");
        subscription.setStatus("ACTIVE");
        subscription.setReservationStatus("NONE");
        subscription.setNextRefillDate(LocalDateTime.now().plusDays(7));
    }

    @Test
    @DisplayName("Phase 3: Dual Notification Cadence - Prescription Expiry Watcher (17-day notice)")
    void testPrescriptionExpiryCadenceWatcher() {
        when(subscriptionRepository.findByStatus("ACTIVE")).thenReturn(List.of(subscription));

        autoRefillScheduler.sendPrescriptionExpiryAlerts();

        verify(notificationService, times(1)).notifyStaffAndPatient(
                eq(patient.getId()),
                contains("Prescription Renewal Due (17 Days Left)"),
                contains("expires in 17 days"),
                contains("Prescription for Patient Rajesh Kumar"),
                eq("WARNING")
        );
    }

    @Test
    @DisplayName("Phase 3: Dual Notification Cadence - Prescription Expiry Watcher (5-day DANGER notice)")
    void testPrescriptionExpiryCadenceDangerNotice() {
        prescription.setExpiryDate(LocalDateTime.now().plusDays(5));
        when(subscriptionRepository.findByStatus("ACTIVE")).thenReturn(List.of(subscription));

        autoRefillScheduler.sendPrescriptionExpiryAlerts();

        verify(notificationService, times(1)).notifyStaffAndPatient(
                eq(patient.getId()),
                contains("Prescription Renewal Due (5 Days Left)"),
                contains("expires in 5 days"),
                contains("Prescription for Patient Rajesh Kumar"),
                eq("DANGER")
        );
    }

    @Test
    @DisplayName("Phase 3: Dual Notification Cadence - Refill Approaching Watcher (7-day advance notice)")
    void testRefillApproachingCadenceWatcher() {
        subscription.setNextRefillDate(LocalDateTime.now().plusDays(7));
        when(subscriptionRepository.findByStatus("ACTIVE")).thenReturn(List.of(subscription));

        autoRefillScheduler.sendRefillApproachingAlerts();

        verify(notificationService, times(1)).notifyStaffAndPatient(
                eq(patient.getId()),
                contains("Upcoming Refill in 7 Day(s)"),
                contains("will dispatch in 7 day(s)"),
                anyString(),
                anyString()
        );
    }

    @Test
    @DisplayName("Phase 3: Soft-Lock Deficit Triggers Immediate Staff Reorder Alert")
    void testSoftLockDeficitTriggersStaffReorderAlert() {
        when(medicineRepository.reserveStock(eq(medicine.getId()), eq(subscription.getQuantity()))).thenReturn(0);

        autoRefillScheduler.reserveInventoryForSubscription(subscription);

        assertEquals("OUT_OF_STOCK_DEFICIT", subscription.getReservationStatus());
        verify(subscriptionRepository, times(1)).save(subscription);
        verify(notificationService, times(1)).notifyStaff(
                contains("🚨 URGENT: Reorder Required"),
                contains("Stockout Warning! Subscription #100"),
                eq("DANGER")
        );
    }

    @Test
    @DisplayName("Phase 3: Closed-Loop Procurement - Get Procurement Alerts with Urgency")
    void testGetProcurementAlertsCategorization() {
        when(medicineRepository.findMedicinesNeedingReorder()).thenReturn(List.of(medicine));
        when(subscriptionRepository.countByMedicineIdAndStatusAndReservationStatus(eq(medicine.getId()), eq("ACTIVE"), eq("OUT_OF_STOCK_DEFICIT")))
                .thenReturn(2L);

        List<ProcurementAlertDTO> alerts = adminService.getProcurementAlerts();

        assertNotNull(alerts);
        assertEquals(1, alerts.size());
        ProcurementAlertDTO alert = alerts.get(0);
        assertEquals(medicine.getId(), alert.getMedicineId());
        assertEquals("Atorvastatin 20mg", alert.getMedicineName());
        assertEquals(15, alert.getAvailableQuantity());
        assertEquals(2L, alert.getDeficitSubscriptionsCount());
        assertEquals("DEFICIT_QUEUED", alert.getUrgency());
    }

    @Test
    @DisplayName("Phase 3: Closed-Loop Procurement - Get Active Deficit Subscriptions")
    void testGetDeficitSubscriptions() {
        subscription.setReservationStatus("OUT_OF_STOCK_DEFICIT");
        when(subscriptionRepository.findByStatusAndReservationStatus("ACTIVE", "OUT_OF_STOCK_DEFICIT"))
                .thenReturn(List.of(subscription));

        List<DeficitSubscriptionDTO> deficits = adminService.getDeficitSubscriptions();

        assertNotNull(deficits);
        assertEquals(1, deficits.size());
        DeficitSubscriptionDTO deficit = deficits.get(0);
        assertEquals(subscription.getId(), deficit.getSubscriptionId());
        assertEquals("Rajesh Kumar", deficit.getPatientName());
        assertEquals(30, deficit.getQuantityNeeded());
    }

    @Test
    @DisplayName("Phase 3: Closed-Loop Restock Automatically Fulfills Waiting Deficits")
    void testRestockWithAutomaticDeficitFulfillment() {
        subscription.setReservationStatus("OUT_OF_STOCK_DEFICIT");
        RestockRequestDTO request = new RestockRequestDTO(medicine.getId(), 50, "BATCH-2026-X", LocalDate.now().plusYears(1));

        when(medicineRepository.findByIdForUpdate(medicine.getId())).thenReturn(Optional.of(medicine));
        when(subscriptionRepository.findByMedicineIdAndStatusAndReservationStatusOrderByNextRefillDateAsc(
                eq(medicine.getId()), eq("ACTIVE"), eq("OUT_OF_STOCK_DEFICIT")
        )).thenReturn(List.of(subscription));
        when(medicineRepository.reserveStock(eq(medicine.getId()), eq(subscription.getQuantity()))).thenReturn(1);
        when(medicineRepository.findById(medicine.getId())).thenReturn(Optional.of(medicine));

        RestockResponseDTO response = adminService.restockMedicine(request);

        assertNotNull(response);
        assertEquals(1, response.getDeficitsResolvedCount());
        assertEquals("RESERVED", subscription.getReservationStatus());
        verify(subscriptionRepository, times(1)).save(subscription);
        verify(notificationService, times(1)).createNotification(
                eq(patient.getId()),
                contains("Medication Restocked & Locked in Vault"),
                contains("has been restocked"),
                eq("SUCCESS")
        );
        verify(notificationService, times(1)).notifyStaff(
                contains("Deficit Fulfilled"),
                contains("Subscription #100"),
                eq("INFO")
        );
    }
}
