package com.automeds.service;

import com.automeds.dto.MedicineDTO;
import com.automeds.dto.SubscriptionResponseDTO;
import com.automeds.entity.*;
import com.automeds.exception.BadRequestException;
import com.automeds.repository.MedicineRepository;
import com.automeds.repository.OrderRepository;
import com.automeds.repository.SubscriptionRepository;
import com.automeds.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class Phase4ResilienceTest {

    private SubscriptionRepository subscriptionRepository;
    private MedicineRepository medicineRepository;
    private UserRepository userRepository;
    private PrescriptionService prescriptionService;
    private NotificationService notificationService;
    private OrderRepository orderRepository;

    private SubscriptionService subscriptionService;
    private MedicineService medicineService;

    private User patient;
    private Medicine medicine;
    private Prescription prescription;
    private Subscription subscription;

    @BeforeEach
    void setUp() {
        subscriptionRepository = mock(SubscriptionRepository.class);
        medicineRepository = mock(MedicineRepository.class);
        userRepository = mock(UserRepository.class);
        prescriptionService = mock(PrescriptionService.class);
        notificationService = mock(NotificationService.class);
        orderRepository = mock(OrderRepository.class);

        subscriptionService = new SubscriptionService(
                subscriptionRepository,
                medicineRepository,
                userRepository,
                prescriptionService,
                notificationService,
                orderRepository
        );

        medicineService = new MedicineService(medicineRepository);

        patient = new User();
        patient.setId(1L);
        patient.setName("Aaditya Aanand");
        patient.setEmail("aaditya@example.com");

        medicine = new Medicine();
        medicine.setId(100L);
        medicine.setMedicineName("Metformin 500mg");
        medicine.setBrandName("Glycomet");
        medicine.setComposition("Metformin");
        medicine.setStrength("500mg");
        medicine.setPrice(new BigDecimal("100.00"));
        medicine.setStockQuantity(50);
        medicine.setReservedQuantity(0);
        medicine.setActive(1);
        medicine.setSymptoms("Diabetes, Blood Sugar, Glycemic Control");

        prescription = new Prescription();
        prescription.setId(50L);
        prescription.setFileName("rx_metformin.pdf");
        prescription.setPatient(patient);
        prescription.setExpiryDate(LocalDateTime.now().minusDays(1)); // Expired

        subscription = new Subscription();
        subscription.setId(10L);
        subscription.setPatient(patient);
        subscription.setMedicine(medicine);
        subscription.setPrescription(prescription);
        subscription.setDosage("1 tablet/day");
        subscription.setFrequency("Daily");
        subscription.setQuantity(30);
        subscription.setStatus("ACTIVE");
        subscription.setNextRefillDate(LocalDateTime.now().plusDays(2));
        subscription.setIsBridgeSupply(false);

        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(subscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void testEmergencyBridgeSupplySuccess() {
        when(medicineRepository.deductAvailableStock(eq(100L), eq(5))).thenReturn(1);

        SubscriptionResponseDTO response = subscriptionService.requestBridgeSupply(1L, 10L);

        assertNotNull(response);
        assertTrue(response.getIsBridgeSupply());

        // Verify order created and inventory deducted
        verify(medicineRepository, times(1)).deductAvailableStock(100L, 5);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(notificationService, times(1)).createNotification(eq(1L), anyString(), anyString(), eq("SUCCESS"));
    }

    @Test
    void testEmergencyBridgeSupplyRejectsDuplicateRequest() {
        subscription.setIsBridgeSupply(true);

        assertThrows(BadRequestException.class, () -> {
            subscriptionService.requestBridgeSupply(1L, 10L);
        });
    }

    @Test
    void testSubscriptionSnoozeReleasesSoftLock() {
        subscription.setReservationStatus("RESERVED");
        subscription.setReservationDate(LocalDateTime.now());
        LocalDateTime initialRefill = subscription.getNextRefillDate();

        SubscriptionResponseDTO response = subscriptionService.snoozeSubscription(1L, 10L, 7);

        assertNotNull(response);
        assertEquals("NONE", response.getReservationStatus());
        assertNull(response.getReservationDate());
        assertEquals(1, response.getSnoozeCount());
        assertNotNull(response.getLastSnoozeDate());
        assertTrue(response.getNextRefillDate().isAfter(initialRefill));

        // Verify reserved stock was released back to general pool
        verify(medicineRepository, times(1)).releaseReservedStock(100L, 30);
    }

    @Test
    void testRefillSynchronizationPillboxDay() {
        Subscription sub2 = new Subscription();
        sub2.setId(11L);
        sub2.setPatient(patient);
        sub2.setMedicine(medicine);
        sub2.setStatus("ACTIVE");
        sub2.setNextRefillDate(LocalDateTime.now().plusDays(15));

        when(subscriptionRepository.findByPatientIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(subscription, sub2));

        List<SubscriptionResponseDTO> synced = subscriptionService.syncRefills(1L, 10);

        assertNotNull(synced);
        assertEquals(2, synced.size());
        assertEquals(10, synced.get(0).getNextRefillDate().getDayOfMonth());
        assertEquals(10, synced.get(1).getNextRefillDate().getDayOfMonth());
    }

    @Test
    void testSymptomSearchAndGenericSavingsEnrichment() {
        Medicine genericMed = new Medicine();
        genericMed.setId(101L);
        genericMed.setMedicineName("Generic Metformin 500mg");
        genericMed.setBrandName("Jan Aushadhi");
        genericMed.setComposition("Metformin");
        genericMed.setStrength("500mg");
        genericMed.setPrice(new BigDecimal("25.00")); // 75% cheaper
        genericMed.setActive(1);

        when(medicineRepository.findBySymptom("diabetes")).thenReturn(List.of(medicine));
        when(medicineRepository.findAlternatives("Metformin", "500mg", 100L)).thenReturn(List.of(genericMed));

        List<MedicineDTO> results = medicineService.getMedicinesBySymptom("diabetes");

        assertNotNull(results);
        assertEquals(1, results.size());
        MedicineDTO dto = results.get(0);
        assertEquals("Diabetes, Blood Sugar, Glycemic Control", dto.getSymptoms());
        assertNotNull(dto.getGenericAlternativeName());
        assertEquals(new BigDecimal("25.00"), dto.getGenericAlternativePrice());
        assertNotNull(dto.getGenericSavingsText());
        assertTrue(dto.getGenericSavingsText().contains("Save 75%"));
    }
}
