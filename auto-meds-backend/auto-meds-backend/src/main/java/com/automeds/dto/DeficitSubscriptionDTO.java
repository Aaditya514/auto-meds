package com.automeds.dto;

import java.time.LocalDateTime;

public class DeficitSubscriptionDTO {
    private Long subscriptionId;
    private Long patientId;
    private String patientName;
    private String patientEmail;
    private Long medicineId;
    private String medicineName;
    private Integer quantityNeeded;
    private Integer currentAvailableStock;
    private LocalDateTime nextRefillDate;
    private Long daysUntilRefill;

    public DeficitSubscriptionDTO() {}

    public DeficitSubscriptionDTO(Long subscriptionId, Long patientId, String patientName, String patientEmail,
                                  Long medicineId, String medicineName, Integer quantityNeeded,
                                  Integer currentAvailableStock, LocalDateTime nextRefillDate, Long daysUntilRefill) {
        this.subscriptionId = subscriptionId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.patientEmail = patientEmail;
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.quantityNeeded = quantityNeeded;
        this.currentAvailableStock = currentAvailableStock;
        this.nextRefillDate = nextRefillDate;
        this.daysUntilRefill = daysUntilRefill;
    }

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(Long subscriptionId) {
        this.subscriptionId = subscriptionId;
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

    public Integer getQuantityNeeded() {
        return quantityNeeded;
    }

    public void setQuantityNeeded(Integer quantityNeeded) {
        this.quantityNeeded = quantityNeeded;
    }

    public Integer getCurrentAvailableStock() {
        return currentAvailableStock;
    }

    public void setCurrentAvailableStock(Integer currentAvailableStock) {
        this.currentAvailableStock = currentAvailableStock;
    }

    public LocalDateTime getNextRefillDate() {
        return nextRefillDate;
    }

    public void setNextRefillDate(LocalDateTime nextRefillDate) {
        this.nextRefillDate = nextRefillDate;
    }

    public Long getDaysUntilRefill() {
        return daysUntilRefill;
    }

    public void setDaysUntilRefill(Long daysUntilRefill) {
        this.daysUntilRefill = daysUntilRefill;
    }
}
