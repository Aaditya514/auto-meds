package com.automeds.service;

import com.automeds.entity.Order;
import com.automeds.entity.OrderItem;
import com.automeds.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @Autowired(required = false)
    private JavaMailSender mailSender;

    /**
     * Sends an email notification when a patient places a new order.
     */
    public void sendOrderConfirmationEmail(User patient, Order order) {
        if (patient == null || patient.getEmail() == null) {
            return;
        }

        String subject = "AutoMeds - Order Confirmation #" + order.getId();
        StringBuilder body = new StringBuilder();
        body.append("Dear ").append(patient.getName()).append(",\n\n");
        body.append("Thank you for your order with AutoMeds! Your order #").append(order.getId()).append(" has been placed successfully.\n\n");
        body.append("--- ORDER SUMMARY ---\n");
        body.append("Order ID: #").append(order.getId()).append("\n");
        body.append("Order Date: ").append(order.getOrderDate().format(DATE_FORMATTER)).append("\n");
        body.append("Expected Delivery: ").append(order.getExpectedDeliveryDate().format(DATE_FORMATTER)).append("\n");
        body.append("Payment Method: ").append(order.getPaymentMethod()).append("\n");
        body.append("Delivery Address: ").append(order.getDeliveryAddress()).append("\n\n");
        body.append("--- ITEMS ORDERED ---\n");

        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                String medName = item.getMedicine() != null ? item.getMedicine().getMedicineName() : "Medicine";
                body.append("• ").append(medName)
                    .append(" x ").append(item.getQuantity())
                    .append(" - ₹").append(item.getSubtotal()).append("\n");
            }
        }
        body.append("\nTOTAL AMOUNT PAID/DUE: ₹").append(order.getTotalAmount()).append("\n\n");
        body.append("We will update you via email as your package is dispatched and out for delivery.\n\n");
        body.append("Best regards,\nAutoMeds Pharmacy Team");

        sendEmail(patient.getEmail(), subject, body.toString());
    }

    /**
     * Sends an email notification whenever an order's status changes (APPROVED, DISPATCHED, DELIVERED, CANCELLED, etc.).
     */
    public void sendOrderStatusUpdateEmail(User patient, Order order, String oldStatus, String newStatus) {
        if (patient == null || patient.getEmail() == null) {
            return;
        }

        String subject = "AutoMeds - Order #" + order.getId() + " Status Update: " + newStatus;
        StringBuilder body = new StringBuilder();
        body.append("Dear ").append(patient.getName()).append(",\n\n");
        body.append("The status of your order #").append(order.getId()).append(" has been updated from ")
            .append(oldStatus != null ? oldStatus : "PENDING").append(" to ").append(newStatus).append(".\n\n");

        if ("SHIPPED".equalsIgnoreCase(newStatus) || "DISPATCHED".equalsIgnoreCase(newStatus)) {
            body.append("Great news! Your medications are packed and on their way to your delivery address:\n");
            body.append(order.getDeliveryAddress()).append("\n");
            body.append("Expected Delivery: ").append(order.getExpectedDeliveryDate().format(DATE_FORMATTER)).append("\n\n");
        } else if ("DELIVERED".equalsIgnoreCase(newStatus)) {
            body.append("Your order #").append(order.getId()).append(" has been delivered successfully. Thank you for choosing AutoMeds!\n\n");
        } else if ("CANCELLED".equalsIgnoreCase(newStatus)) {
            body.append("Your order #").append(order.getId()).append(" has been cancelled.\n\n");
        } else {
            body.append("Current Order Status: ").append(newStatus).append("\n\n");
        }

        body.append("You can track your order status anytime by logging into your AutoMeds patient portal.\n\n");
        body.append("Best regards,\nAutoMeds Pharmacy Team");

        sendEmail(patient.getEmail(), subject, body.toString());
    }

    /**
     * Sends an email when an auto-refill subscription order is generated.
     */
    public void sendSubscriptionRefillEmail(User patient, Order order) {
        if (patient == null || patient.getEmail() == null) {
            return;
        }

        String subject = "AutoMeds - Auto-Refill Order #" + order.getId() + " Generated";
        StringBuilder body = new StringBuilder();
        body.append("Dear ").append(patient.getName()).append(",\n\n");
        body.append("Your recurring chronic medication subscription auto-refill order #").append(order.getId())
            .append(" has been generated automatically!\n\n");
        body.append("Expected Refill Delivery Date: ").append(order.getExpectedDeliveryDate().format(DATE_FORMATTER)).append("\n");
        body.append("Total Amount: ₹").append(order.getTotalAmount()).append("\n");
        body.append("Delivery Address: ").append(order.getDeliveryAddress()).append("\n\n");
        body.append("Best regards,\nAutoMeds Pharmacy Team");

        sendEmail(patient.getEmail(), subject, body.toString());
    }

    /**
     * Sends general subscription status emails (Clarification Required, Approved, Rejected).
     */
    public void sendSubscriptionNotificationEmail(User patient, String title, String message) {
        if (patient == null || patient.getEmail() == null) {
            return;
        }

        String subject = "AutoMeds - " + title;
        StringBuilder body = new StringBuilder();
        body.append("Dear ").append(patient.getName()).append(",\n\n");
        body.append(message).append("\n\n");
        body.append("Please log into your AutoMeds account for more details.\n\n");
        body.append("Best regards,\nAutoMeds Pharmacy Team");

        sendEmail(patient.getEmail(), subject, body.toString());
    }

    /**
     * Core email dispatcher method (logs formatted output for server logs).
     */
    private void sendEmail(String toEmail, String subject, String body) {
        logger.info("==========================================================");
        logger.info(">>> DISPATCHING AUTOMATED EMAIL NOTIFICATION <<<");
        logger.info("TO: {}", toEmail);
        logger.info("SUBJECT: {}", subject);
        logger.info("BODY:\n{}", body);
        logger.info("==========================================================");

        if (mailSender != null) {
            try {
                SimpleMailMessage mailMessage = new SimpleMailMessage();
                mailMessage.setTo(toEmail);
                mailMessage.setSubject(subject);
                mailMessage.setText(body);
                mailSender.send(mailMessage);
                logger.info(">>> Real email successfully sent via SMTP to {} <<<", toEmail);
            } catch (Exception e) {
                logger.warn(">>> Network SMTP email dispatch skipped/failed (requires valid SMTP server in application.properties): {} <<<", e.getMessage());
            }
        }
    }
}
