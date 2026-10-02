package com.automeds.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class RestockRequestDTO {
    @NotNull(message = "Medicine ID is required")
    private Long medicineId;

    @NotNull(message = "Restock quantity is required")
    @Min(value = 1, message = "Restock quantity must be at least 1")
    private Integer quantity;

    private String batchNumber;
    private LocalDate expiryDate;

    public RestockRequestDTO() {}

    public RestockRequestDTO(Long medicineId, Integer quantity, String batchNumber, LocalDate expiryDate) {
        this.medicineId = medicineId;
        this.quantity = quantity;
        this.batchNumber = batchNumber;
        this.expiryDate = expiryDate;
    }

    public Long getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Long medicineId) {
        this.medicineId = medicineId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }
}
