package com.automeds.dto;

public class RestockResponseDTO {
    private Long medicineId;
    private String medicineName;
    private Integer previousStock;
    private Integer newStock;
    private Integer previousAvailable;
    private Integer newAvailable;
    private Integer deficitsResolvedCount;
    private String message;

    public RestockResponseDTO() {}

    public RestockResponseDTO(Long medicineId, String medicineName, Integer previousStock, Integer newStock,
                              Integer previousAvailable, Integer newAvailable, Integer deficitsResolvedCount,
                              String message) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.previousStock = previousStock;
        this.newStock = newStock;
        this.previousAvailable = previousAvailable;
        this.newAvailable = newAvailable;
        this.deficitsResolvedCount = deficitsResolvedCount;
        this.message = message;
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

    public Integer getPreviousStock() {
        return previousStock;
    }

    public void setPreviousStock(Integer previousStock) {
        this.previousStock = previousStock;
    }

    public Integer getNewStock() {
        return newStock;
    }

    public void setNewStock(Integer newStock) {
        this.newStock = newStock;
    }

    public Integer getPreviousAvailable() {
        return previousAvailable;
    }

    public void setPreviousAvailable(Integer previousAvailable) {
        this.previousAvailable = previousAvailable;
    }

    public Integer getNewAvailable() {
        return newAvailable;
    }

    public void setNewAvailable(Integer newAvailable) {
        this.newAvailable = newAvailable;
    }

    public Integer getDeficitsResolvedCount() {
        return deficitsResolvedCount;
    }

    public void setDeficitsResolvedCount(Integer deficitsResolvedCount) {
        this.deficitsResolvedCount = deficitsResolvedCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
