package com.automeds.service;

import com.automeds.dto.DispensingSlipDTO;
import com.automeds.dto.DrugInteractionAlertDTO;
import com.automeds.dto.PaymentRequestDTO;
import com.automeds.dto.PaymentResponseDTO;
import com.automeds.entity.*;
import com.automeds.repository.AuditLogRepository;
import com.automeds.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class Phase5ComplianceAndPaymentTest {

    private AuditLogRepository auditLogRepository;
    private AuditLogService auditLogService;

    private DrugInteractionService drugInteractionService;

    private OrderRepository orderRepository;
    private NotificationService notificationService;
    private EmailService emailService;
    private PrescriptionOcrService prescriptionOcrService;
    private PaymentService paymentService;

    private User patient;
    private Order order;

    @BeforeEach
    void setUp() {
        auditLogRepository = mock(AuditLogRepository.class);
        auditLogService = new AuditLogService(auditLogRepository);

        drugInteractionService = new DrugInteractionService();

        orderRepository = mock(OrderRepository.class);
        notificationService = mock(NotificationService.class);
        emailService = mock(EmailService.class);
        prescriptionOcrService = mock(PrescriptionOcrService.class);

        paymentService = new PaymentService(
                orderRepository,
                auditLogService,
                notificationService,
                emailService,
                prescriptionOcrService
        );

        patient = new User();
        patient.setId(1L);
        patient.setName("Aaditya Test");
        patient.setEmail("aaditya@test.com");
        patient.setPhone("+919876543210");

        order = new Order();
        order.setId(101L);
        order.setPatient(patient);
        order.setTotalAmount(new BigDecimal("350.00"));
        order.setPaymentStatus("PENDING");
        order.setOrderStatus("PENDING");
        order.setOrderDate(LocalDateTime.now());
        order.setDeliveryAddress("123 Health Street, City");

        when(orderRepository.findById(101L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void testAuditLogRecording() {
        AuditLog log = auditLogService.recordLog(
                1L, "Dr. Sarah", "PHARMACIST", "PRESCRIPTION_VERIFIED", 
                "PRESCRIPTION", 55L, "Approved Metformin 500mg", "192.168.1.1"
        );

        assertNotNull(log);
        assertEquals("PRESCRIPTION_VERIFIED", log.getAction());
        assertEquals("PHARMACIST", log.getActorRole());
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void testDrugInteractionDetection_AmlodipineAndSimvastatin() {
        Medicine amlodipine = new Medicine();
        amlodipine.setMedicineName("Amlodipine 5mg");
        amlodipine.setComposition("Amlodipine Besylate");

        Medicine simvastatin = new Medicine();
        simvastatin.setMedicineName("Simvastatin 20mg");
        simvastatin.setComposition("Simvastatin");

        List<DrugInteractionAlertDTO> alerts = drugInteractionService.checkInteractions(
                List.of(amlodipine, simvastatin), null
        );

        assertFalse(alerts.isEmpty());
        DrugInteractionAlertDTO alert = alerts.get(0);
        assertEquals("HIGH", alert.getSeverity());
        assertEquals("DRUG_DRUG", alert.getAlertType());
        assertTrue(alert.getClinicalEffect().contains("myopathy"));
    }

    @Test
    void testDrugAllergyDetection_PenicillinAllergy() {
        Medicine amoxicillin = new Medicine();
        amoxicillin.setMedicineName("Amoxicillin 500mg Capsule");
        amoxicillin.setComposition("Amoxicillin Trihydrate");

        List<DrugInteractionAlertDTO> alerts = drugInteractionService.checkInteractions(
                List.of(amoxicillin), "Penicillin, Sulfa"
        );

        // Amoxicillin contains 'cillin'
        assertFalse(alerts.isEmpty());
        DrugInteractionAlertDTO alert = alerts.get(0);
        assertEquals("ALLERGY_CONFLICT", alert.getAlertType());
        assertEquals("HIGH", alert.getSeverity());
    }

    @Test
    void testProcessPayment_UPIOnlinePayment() {
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setPaymentMethod("UPI");
        request.setUpiId("aaditya@okaxis");

        PaymentResponseDTO response = paymentService.processPayment(101L, request);

        assertNotNull(response);
        assertEquals("PAID", response.getPaymentStatus());
        assertEquals("UPI", response.getPaymentMethod());
        assertNotNull(response.getTransactionId());
        assertTrue(response.getTransactionId().startsWith("TXN_"));
        assertNotNull(response.getDispensingSlipCode());
        verify(notificationService, times(1)).createNotification(eq(1L), anyString(), anyString(), eq("SUCCESS"));
    }

    @Test
    void testProcessPayment_CashOnDelivery() {
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setPaymentMethod("CASH_ON_DELIVERY");

        PaymentResponseDTO response = paymentService.processPayment(101L, request);

        assertNotNull(response);
        assertEquals("PENDING_COD", response.getPaymentStatus());
        assertEquals("CASH_ON_DELIVERY", response.getPaymentMethod());
    }

    @Test
    void testGetDispensingSlip_FourEyesCertified() {
        DispensingSlipDTO slip = paymentService.getDispensingSlip(101L);

        assertNotNull(slip);
        assertEquals(101L, slip.getOrderId());
        assertTrue(slip.isFourEyesCertified());
        assertNotNull(slip.getDispensingPharmacistName());
        assertNotNull(slip.getQrCodePayload());
        assertTrue(slip.getQrCodePayload().contains("automeds.com/verify-prescription"));
    }
}
