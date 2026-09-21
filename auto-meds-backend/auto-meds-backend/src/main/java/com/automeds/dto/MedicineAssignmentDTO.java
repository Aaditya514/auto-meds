package com.automeds.dto;

/**
 * DTO representing a medicine assignment by a pharmacist during subscription approval.
 */
public class MedicineAssignmentDTO {
    private Long medicineId;
    private String dosage;
    private String frequency;
    private Integer quantity;

    public MedicineAssignmentDTO() {
    }

    public MedicineAssignmentDTO(Long medicineId, String dosage, String frequency, Integer quantity) {
        this.medicineId = medicineId;
        this.dosage = dosage;
        this.frequency = frequency;
        this.quantity = quantity;
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
}
