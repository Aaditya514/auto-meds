package com.automeds.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class DispensingSlipDTO {

    private Long orderId;
    private String dispensingSlipCode;
    private String qrCodePayload;
    private LocalDateTime orderDate;
    private LocalDateTime issuedAt;

    // Patient Details
    private String patientName;
    private String patientPhone;
    private String deliveryAddress;

    // Clinical Prescription Reference
    private Long prescriptionId;
    private String doctorName;
    private String doctorRegNumber;
    private String clinicOrHospital;

    // Pharmacist Verification Authority
    private String dispensingPharmacistName;
    private String pharmacistLicenseNumber;
    private boolean fourEyesCertified;

    // Financial
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String paymentStatus;
    private String transactionId;

    // Itemized Medicines
    private List<OrderItemDTO> items;

    public DispensingSlipDTO() {
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getDispensingSlipCode() {
        return dispensingSlipCode;
    }

    public void setDispensingSlipCode(String dispensingSlipCode) {
        this.dispensingSlipCode = dispensingSlipCode;
    }

    public String getQrCodePayload() {
        return qrCodePayload;
    }

    public void setQrCodePayload(String qrCodePayload) {
        this.qrCodePayload = qrCodePayload;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public Long getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(Long prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorRegNumber() {
        return doctorRegNumber;
    }

    public void setDoctorRegNumber(String doctorRegNumber) {
        this.doctorRegNumber = doctorRegNumber;
    }

    public String getClinicOrHospital() {
        return clinicOrHospital;
    }

    public void setClinicOrHospital(String clinicOrHospital) {
        this.clinicOrHospital = clinicOrHospital;
    }

    public String getDispensingPharmacistName() {
        return dispensingPharmacistName;
    }

    public void setDispensingPharmacistName(String dispensingPharmacistName) {
        this.dispensingPharmacistName = dispensingPharmacistName;
    }

    public String getPharmacistLicenseNumber() {
        return pharmacistLicenseNumber;
    }

    public void setPharmacistLicenseNumber(String pharmacistLicenseNumber) {
        this.pharmacistLicenseNumber = pharmacistLicenseNumber;
    }

    public boolean isFourEyesCertified() {
        return fourEyesCertified;
    }

    public void setFourEyesCertified(boolean fourEyesCertified) {
        this.fourEyesCertified = fourEyesCertified;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
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

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public List<OrderItemDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDTO> items) {
        this.items = items;
    }
}
