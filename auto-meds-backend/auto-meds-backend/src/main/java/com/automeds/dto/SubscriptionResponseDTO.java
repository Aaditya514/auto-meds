package com.automeds.dto;

import java.time.LocalDateTime;

/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: SubscriptionResponseDTO
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class SubscriptionResponseDTO {

    private Long id;
    private Long patientId;
    private String patientName;
    private String patientEmail;
    private Long medicineId;
    private String medicineName;
    private String brandName;
    private String composition;
    private String strength;
    private Long prescriptionId;
    private String prescriptionFileName;
    private LocalDateTime prescriptionExpiryDate;
    private String dosage;
    private String frequency;
    private Integer quantity;
    private LocalDateTime startDate;
    private LocalDateTime nextRefillDate;
    private LocalDateTime nextDispatchDate;
    private String status;
    private LocalDateTime createdAt;

    public SubscriptionResponseDTO() {
    }

    public SubscriptionResponseDTO(Long id, Long patientId, String patientName, String patientEmail, Long medicineId, String medicineName, String brandName, String composition, String strength, Long prescriptionId, String prescriptionFileName, LocalDateTime prescriptionExpiryDate, String dosage, String frequency, Integer quantity, LocalDateTime startDate, LocalDateTime nextRefillDate, LocalDateTime nextDispatchDate, String status, LocalDateTime createdAt) {
        this.id = id;
        this.patientId = patientId;
        this.patientName = patientName;
        this.patientEmail = patientEmail;
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.brandName = brandName;
        this.composition = composition;
        this.strength = strength;
        this.prescriptionId = prescriptionId;
        this.prescriptionFileName = prescriptionFileName;
        this.prescriptionExpiryDate = prescriptionExpiryDate;
        this.dosage = dosage;
        this.frequency = frequency;
        this.quantity = quantity;
        this.startDate = startDate;
        this.nextRefillDate = nextRefillDate;
        this.nextDispatchDate = nextDispatchDate;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public Long getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Long medicineId) {
        this.medicineId = medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getComposition() {
        return composition;
    }

    public void setComposition(String composition) {
        this.composition = composition;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public Long getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(Long prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public String getPrescriptionFileName() {
        return prescriptionFileName;
    }

    public void setPrescriptionFileName(String prescriptionFileName) {
        this.prescriptionFileName = prescriptionFileName;
    }

    public LocalDateTime getPrescriptionExpiryDate() {
        return prescriptionExpiryDate;
    }

    public void setPrescriptionExpiryDate(LocalDateTime prescriptionExpiryDate) {
        this.prescriptionExpiryDate = prescriptionExpiryDate;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getNextRefillDate() {
        return nextRefillDate;
    }

    public void setNextRefillDate(LocalDateTime nextRefillDate) {
        this.nextRefillDate = nextRefillDate;
    }

    public LocalDateTime getNextDispatchDate() {
        return nextDispatchDate;
    }

    public void setNextDispatchDate(LocalDateTime nextDispatchDate) {
        this.nextDispatchDate = nextDispatchDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "SubscriptionResponseDTO{" +
                "id=" + id +
                ", patientName='" + patientName + '\'' +
                ", medicineName='" + medicineName + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
