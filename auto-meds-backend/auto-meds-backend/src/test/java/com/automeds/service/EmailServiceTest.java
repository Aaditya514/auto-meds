package com.automeds.service;

import com.automeds.entity.Order;
import com.automeds.entity.OrderItem;
import com.automeds.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class EmailServiceTest {

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService();
    }

    @Test
    void testSendOrderConfirmationEmail() {
        User patient = new User();
        patient.setId(1L);
        patient.setName("Aaditya Aanand");
        patient.setEmail("aaditya.aanand@intellectdesign.com");

        Order order = new Order();
        order.setId(101L);
        order.setOrderDate(LocalDateTime.now());
        order.setExpectedDeliveryDate(LocalDateTime.now().plusDays(3));
        order.setPaymentMethod("CREDIT_CARD");
        order.setDeliveryAddress("123 Health Street, City");
        order.setTotalAmount(new BigDecimal("290.00"));
        order.setItems(new ArrayList<>());

        assertDoesNotThrow(() -> emailService.sendOrderConfirmationEmail(patient, order));
    }

    @Test
    void testSendOrderStatusUpdateEmail() {
        User patient = new User();
        patient.setId(1L);
        patient.setName("Aaditya Aanand");
        patient.setEmail("aaditya.aanand@intellectdesign.com");

        Order order = new Order();
        order.setId(101L);
        order.setExpectedDeliveryDate(LocalDateTime.now().plusDays(3));
        order.setDeliveryAddress("123 Health Street, City");

        assertDoesNotThrow(() -> emailService.sendOrderStatusUpdateEmail(patient, order, "APPROVED", "DISPATCHED"));
    }

    @Test
    void testSendSubscriptionRefillEmail() {
        User patient = new User();
        patient.setId(1L);
        patient.setName("Aaditya Aanand");
        patient.setEmail("aaditya.aanand@intellectdesign.com");

        Order order = new Order();
        order.setId(102L);
        order.setExpectedDeliveryDate(LocalDateTime.now().plusDays(3));
        order.setDeliveryAddress("123 Health Street, City");
        order.setTotalAmount(new BigDecimal("150.00"));

        assertDoesNotThrow(() -> emailService.sendSubscriptionRefillEmail(patient, order));
    }

    @Test
    void testSendSubscriptionNotificationEmail() {
        User patient = new User();
        patient.setId(1L);
        patient.setName("Aaditya Aanand");
        patient.setEmail("aaditya.aanand@intellectdesign.com");

        assertDoesNotThrow(() -> emailService.sendSubscriptionNotificationEmail(patient, "Clarification Required", "Please upload a clearer prescription."));
    }

    @Test
    void testSendEmailNullUserOrEmail() {
        assertDoesNotThrow(() -> emailService.sendOrderConfirmationEmail(null, new Order()));
        
        User nullEmailUser = new User();
        nullEmailUser.setEmail(null);
        assertDoesNotThrow(() -> emailService.sendOrderConfirmationEmail(nullEmailUser, new Order()));
    }
}
