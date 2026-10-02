package com.automeds.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponseDTO {

    private Long orderId;
    private String transactionId;
    private String receiptNumber;
    private BigDecimal amount;
    private String paymentMethod;
    private String paymentStatus; // SUCCESS, PENDING_COD, FAILED
    private String message;
    private LocalDateTime paidAt;
    private String dispensingSlipCode;

    public PaymentResponseDTO() {
    }

    public PaymentResponseDTO(Long orderId, String transactionId, String receiptNumber, 
                              BigDecimal amount, String paymentMethod, String paymentStatus, 
                              String message, LocalDateTime paidAt, String dispensingSlipCode) {
        this.orderId = orderId;
        this.transactionId = transactionId;
        this.receiptNumber = receiptNumber;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.message = message;
        this.paidAt = paidAt;
        this.dispensingSlipCode = dispensingSlipCode;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public String getDispensingSlipCode() {
        return dispensingSlipCode;
    }

    public void setDispensingSlipCode(String dispensingSlipCode) {
        this.dispensingSlipCode = dispensingSlipCode;
    }
}
