package com.automeds.service;

import com.automeds.dto.SubscriptionRequestDTO;
import com.automeds.dto.SubscriptionResponseDTO;
import com.automeds.entity.Medicine;
import com.automeds.entity.Prescription;
import com.automeds.entity.Subscription;
import com.automeds.entity.User;
import com.automeds.exception.BadRequestException;
import com.automeds.exception.ResourceNotFoundException;
import com.automeds.repository.MedicineRepository;

import com.automeds.repository.SubscriptionRepository;
import com.automeds.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class SubscriptionServiceTest {

    private SubscriptionRepository subscriptionRepository;
    private MedicineRepository medicineRepository;
    private UserRepository userRepository;
    private PrescriptionService prescriptionService;
    private NotificationService notificationService;
    private SubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        subscriptionRepository = Mockito.mock(SubscriptionRepository.class);
        medicineRepository = Mockito.mock(MedicineRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        prescriptionService = Mockito.mock(PrescriptionService.class);
        notificationService = Mockito.mock(NotificationService.class);

        subscriptionService = new SubscriptionService(subscriptionRepository, medicineRepository, userRepository, prescriptionService, notificationService);
    }

    @Test
    void testCreateSubscriptionSuccess() {
        User patient = new User();
        patient.setId(1L);
        patient.setName("User");

        Medicine m = new Medicine();
        m.setId(2L);
        m.setMedicineName("Paracetamol");

        Prescription p = new Prescription();
        p.setId(3L);

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        Mockito.when(medicineRepository.findById(2L)).thenReturn(Optional.of(m));
        MockMultipartFile file = new MockMultipartFile("file", "p.pdf", "application/pdf", "data".getBytes());
        Mockito.when(prescriptionService.uploadPrescription(any(), any(), any(), any())).thenReturn(p);

        Subscription sub = new Subscription();
        sub.setId(10L);
        sub.setPatient(patient);
        sub.setMedicine(m);
        sub.setPrescription(p);
        sub.setStatus("PENDING");

        Mockito.when(subscriptionRepository.save(any())).thenReturn(sub);

        SubscriptionRequestDTO req = new SubscriptionRequestDTO(2L, "1 tablet", "Daily", 30);
        SubscriptionResponseDTO res = subscriptionService.createSubscription(1L, req, file);

        assertNotNull(res);
        assertEquals(10L, res.getId());
    }

    @Test
    void testCreateSubscriptionUserNotFound() {
        Mockito.when(userRepository.findById(99L)).thenReturn(Optional.empty());
        SubscriptionRequestDTO req = new SubscriptionRequestDTO(2L, "1 tablet", "Daily", 30);
        MockMultipartFile file = new MockMultipartFile("file", "p.pdf", "application/pdf", "data".getBytes());
        assertThrows(ResourceNotFoundException.class, () -> subscriptionService.createSubscription(99L, req, file));
    }

    @Test
    void testCreateSubscriptionWithoutMedicine() {
        User patient = new User();
        patient.setId(1L);
        patient.setName("User");

        Prescription p = new Prescription();
        p.setId(3L);

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        MockMultipartFile file = new MockMultipartFile("file", "p.pdf", "application/pdf", "data".getBytes());
        Mockito.when(prescriptionService.uploadPrescription(any(), any(), any(), any())).thenReturn(p);

        Subscription sub = new Subscription();
        sub.setId(10L);
        sub.setPatient(patient);
        sub.setPrescription(p);
        sub.setStatus("PENDING");

        Mockito.when(subscriptionRepository.save(any())).thenReturn(sub);

        SubscriptionRequestDTO req = new SubscriptionRequestDTO(null, "1 tablet", "Daily", 30);
        SubscriptionResponseDTO res = subscriptionService.createSubscription(1L, req, file);

        assertNotNull(res);
        assertEquals(10L, res.getId());
        assertEquals("Pending Pharmacist Review", res.getMedicineName());
    }

    @Test
    void testCreateSubscriptionMissingFile() {
        User patient = new User();
        patient.setId(1L);
        Medicine m = new Medicine();
        m.setId(2L);

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        Mockito.when(medicineRepository.findById(2L)).thenReturn(Optional.of(m));

        SubscriptionRequestDTO req = new SubscriptionRequestDTO(2L, "1 tablet", "Daily", 30);
        assertThrows(BadRequestException.class, () -> subscriptionService.createSubscription(1L, req, null));
    }

    @Test
    void testGetSubscriptionsForPatient() {
        User patient = new User();
        patient.setId(1L);
        Medicine m = new Medicine();
        m.setId(2L);

        Subscription sub = new Subscription();
        sub.setId(10L);
        sub.setPatient(patient);
        sub.setMedicine(m);

        Mockito.when(subscriptionRepository.findByPatientIdOrderByCreatedAtDesc(1L)).thenReturn(Collections.singletonList(sub));
        List<SubscriptionResponseDTO> list = subscriptionService.getSubscriptionsForPatient(1L);
        assertEquals(1, list.size());
    }

    @Test
    void testGetSubscriptionById() {
        User patient = new User();
        patient.setId(1L);
        Medicine m = new Medicine();
        m.setId(2L);

        Subscription sub = new Subscription();
        sub.setId(10L);
        sub.setPatient(patient);
        sub.setMedicine(m);

        Mockito.when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(sub));
        SubscriptionResponseDTO dto = subscriptionService.getSubscriptionById(10L);
        assertEquals(10L, dto.getId());
    }

    @Test
    void testGetSubscriptionByIdNotFound() {
        Mockito.when(subscriptionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> subscriptionService.getSubscriptionById(99L));
    }

    @Test
    void testCancelSubscription() {
        User patient = new User();
        patient.setId(1L);
        Medicine m = new Medicine();

        Subscription sub = new Subscription();
        sub.setId(10L);
        sub.setPatient(patient);
        sub.setMedicine(m);
        sub.setStatus("ACTIVE");

        Mockito.when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(sub));
        Mockito.when(subscriptionRepository.save(sub)).thenReturn(sub);

        SubscriptionResponseDTO res = subscriptionService.cancelSubscription(1L, 10L);
        assertEquals("CANCELLED", res.getStatus());
    }

    @Test
    void testCancelSubscriptionUnauthorized() {
        User patient = new User();
        patient.setId(1L);
        Subscription sub = new Subscription();
        sub.setId(10L);
        sub.setPatient(patient);

        Mockito.when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(sub));
        assertThrows(BadRequestException.class, () -> subscriptionService.cancelSubscription(2L, 10L));
    }

    @Test
    void testPauseAndResumeSubscription() {
        User patient = new User();
        patient.setId(1L);
        Medicine m = new Medicine();

        Subscription sub = new Subscription();
        sub.setId(10L);
        sub.setPatient(patient);
        sub.setMedicine(m);
        sub.setStatus("ACTIVE");

        Mockito.when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(sub));
        Mockito.when(subscriptionRepository.save(sub)).thenReturn(sub);

        SubscriptionResponseDTO paused = subscriptionService.pauseSubscription(1L, 10L);
        assertEquals("PAUSED", paused.getStatus());

        SubscriptionResponseDTO resumed = subscriptionService.resumeSubscription(1L, 10L);
        assertEquals("ACTIVE", resumed.getStatus());
    }

    @Test
    void testPauseSubscriptionUnauthorizedAndInvalidState() {
        User patient = new User();
        patient.setId(1L);
        Subscription sub = new Subscription();
        sub.setId(10L);
        sub.setPatient(patient);
        sub.setStatus("PENDING");

        Mockito.when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(sub));
        assertThrows(BadRequestException.class, () -> subscriptionService.pauseSubscription(2L, 10L));
        assertThrows(BadRequestException.class, () -> subscriptionService.pauseSubscription(1L, 10L));
    }

    @Test
    void testResumeSubscriptionUnauthorizedAndInvalidState() {
        User patient = new User();
        patient.setId(1L);
        Subscription sub = new Subscription();
        sub.setId(10L);
        sub.setPatient(patient);
        sub.setStatus("ACTIVE");

        Mockito.when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(sub));
        assertThrows(BadRequestException.class, () -> subscriptionService.resumeSubscription(2L, 10L));
        assertThrows(BadRequestException.class, () -> subscriptionService.resumeSubscription(1L, 10L));
    }

    @Test
    void testRenewSubscriptionWithNewPrescription() {
        User patient = new User();
        patient.setId(1L);
        Medicine m = new Medicine();

        Subscription sub = new Subscription();
        sub.setId(10L);
        sub.setPatient(patient);
        sub.setMedicine(m);

        Prescription newP = new Prescription();
        newP.setId(50L);

        MockMultipartFile file = new MockMultipartFile("file", "new.pdf", "application/pdf", "data".getBytes());
        Mockito.when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(sub));
        Mockito.when(prescriptionService.uploadPrescription(any(), any(), any(), any())).thenReturn(newP);
        Mockito.when(subscriptionRepository.save(sub)).thenReturn(sub);

        SubscriptionResponseDTO res = subscriptionService.renewSubscription(1L, 10L, file);
        assertEquals("PENDING", res.getStatus());
    }

    @Test
    void testRenewSubscriptionWithoutNewPrescription() {
        User patient = new User();
        patient.setId(1L);
        Medicine m = new Medicine();

        Subscription sub = new Subscription();
        sub.setId(10L);
        sub.setPatient(patient);
        sub.setMedicine(m);

        Mockito.when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(sub));
        Mockito.when(subscriptionRepository.save(sub)).thenReturn(sub);

        SubscriptionResponseDTO res = subscriptionService.renewSubscription(1L, 10L, null);
        assertEquals("PENDING", res.getStatus());
    }

    @Test
    void testRenewSubscriptionUnauthorized() {
        User patient = new User();
        patient.setId(1L);
        Subscription sub = new Subscription();
        sub.setId(10L);
        sub.setPatient(patient);

        Mockito.when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(sub));
        assertThrows(BadRequestException.class, () -> subscriptionService.renewSubscription(2L, 10L, null));
    }

    @Test
    void testCalculateDurationDays() {
        assertEquals(30, subscriptionService.calculateDurationDays(30, "1 tablet/day"));
        assertEquals(15, subscriptionService.calculateDurationDays(30, "2 tablets/day"));
        assertEquals(30, subscriptionService.calculateDurationDays(30, null));
        assertEquals(30, subscriptionService.calculateDurationDays(30, "no-numbers-here"));
    }
}


