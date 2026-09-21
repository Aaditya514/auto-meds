package com.automeds.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: SubscriptionRequestDTO
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class SubscriptionRequestDTO {

    private Long medicineId;

    private String dosage; // e.g. "1 tablet/day"

    private String frequency; // e.g. "Once Daily"

    private Integer quantity; // e.g. 30

    private java.time.LocalDateTime doctorVisitDate;

    public SubscriptionRequestDTO() {
    }

    public SubscriptionRequestDTO(Long medicineId, String dosage, String frequency, Integer quantity) {
        this.medicineId = medicineId;
        this.dosage = dosage;
        this.frequency = frequency;
        this.quantity = quantity;
    }

    public SubscriptionRequestDTO(Long medicineId, String dosage, String frequency, Integer quantity, java.time.LocalDateTime doctorVisitDate) {
        this.medicineId = medicineId;
        this.dosage = dosage;
        this.frequency = frequency;
        this.quantity = quantity;
        this.doctorVisitDate = doctorVisitDate;
    }

    public Long getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Long medicineId) {
        this.medicineId = medicineId;
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

    public java.time.LocalDateTime getDoctorVisitDate() {
        return doctorVisitDate;
    }

    public void setDoctorVisitDate(java.time.LocalDateTime doctorVisitDate) {
        this.doctorVisitDate = doctorVisitDate;
    }

    @Override
    public String toString() {
        return "SubscriptionRequestDTO{" +
                "medicineId=" + medicineId +
                ", dosage='" + dosage + '\'' +
                ", frequency='" + frequency + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}
