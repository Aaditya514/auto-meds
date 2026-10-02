package com.automeds.dto;

import java.math.BigDecimal;

public class ProcurementAlertDTO {
    private Long medicineId;
    private String medicineName;
    private String brandName;
    private String composition;
    private String strength;
    private BigDecimal price;
    private Integer stockQuantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private Integer reorderThreshold;
    private Integer suggestedReorderPackSize;
    private Long deficitSubscriptionsCount;
    private String urgency; // CRITICAL_STOCKOUT, DEFICIT_QUEUED, LOW_STOCK

    public ProcurementAlertDTO() {}

    public ProcurementAlertDTO(Long medicineId, String medicineName, String brandName, String composition,
                               String strength, BigDecimal price, Integer stockQuantity, Integer reservedQuantity,
                               Integer availableQuantity, Integer reorderThreshold, Integer suggestedReorderPackSize,
                               Long deficitSubscriptionsCount, String urgency) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.brandName = brandName;
        this.composition = composition;
        this.strength = strength;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.reservedQuantity = reservedQuantity;
        this.availableQuantity = availableQuantity;
        this.reorderThreshold = reorderThreshold;
        this.suggestedReorderPackSize = suggestedReorderPackSize;
        this.deficitSubscriptionsCount = deficitSubscriptionsCount;
        this.urgency = urgency;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Integer getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(Integer reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public Integer getReorderThreshold() {
        return reorderThreshold;
    }

    public void setReorderThreshold(Integer reorderThreshold) {
        this.reorderThreshold = reorderThreshold;
    }

    public Integer getSuggestedReorderPackSize() {
        return suggestedReorderPackSize;
    }

    public void setSuggestedReorderPackSize(Integer suggestedReorderPackSize) {
        this.suggestedReorderPackSize = suggestedReorderPackSize;
    }

    public Long getDeficitSubscriptionsCount() {
        return deficitSubscriptionsCount;
    }

    public void setDeficitSubscriptionsCount(Long deficitSubscriptionsCount) {
        this.deficitSubscriptionsCount = deficitSubscriptionsCount;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }
}
