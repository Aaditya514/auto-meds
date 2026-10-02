package com.automeds.dto;

import java.math.BigDecimal;

public class PrescriptionOcrCandidateDTO {
    private String detectedText;
    private Long medicineId;
    private String medicineName;
    private String brandName;
    private String composition;
    private String strength;
    private String dosage;
    private String frequency;
    private Integer quantity;
    private BigDecimal price;
    private Double confidenceScore;
    private String confidenceBadge; // HIGH_CONFIDENCE, MEDIUM_CONFIDENCE, LOW_CONFIDENCE
    private String genericAlternative;
    private BigDecimal genericPrice;
    private Integer savingsPercentage;

    public PrescriptionOcrCandidateDTO() {
    }

    public String getDetectedText() {
        return detectedText;
    }

    public void setDetectedText(String detectedText) {
        this.detectedText = detectedText;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getConfidenceBadge() {
        return confidenceBadge;
    }

    public void setConfidenceBadge(String confidenceBadge) {
        this.confidenceBadge = confidenceBadge;
    }

    public String getGenericAlternative() {
        return genericAlternative;
    }

    public void setGenericAlternative(String genericAlternative) {
        this.genericAlternative = genericAlternative;
    }

    public BigDecimal getGenericPrice() {
        return genericPrice;
    }

    public void setGenericPrice(BigDecimal genericPrice) {
        this.genericPrice = genericPrice;
    }

    public Integer getSavingsPercentage() {
        return savingsPercentage;
    }

    public void setSavingsPercentage(Integer savingsPercentage) {
        this.savingsPercentage = savingsPercentage;
    }
}
