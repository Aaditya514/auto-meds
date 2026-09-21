package com.automeds.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: MedicineDTO
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class MedicineDTO {

    private Long id;
    private String medicineName;
    private String brandName;
    private String composition;
    private String strength;
    private String category;
    private BigDecimal price;
    private Integer stockQuantity;
    private Boolean requiresPrescription;
    private String description;
    private String manufacturer;
    private LocalDateTime expiryDate;
    private Boolean active;
    private Boolean inStock;

    public MedicineDTO() {
    }

    @SuppressWarnings("java:S107")
    public MedicineDTO(Long id, String medicineName, String brandName, String composition, String strength, String category, BigDecimal price, Integer stockQuantity, Boolean requiresPrescription, String description, String manufacturer, LocalDateTime expiryDate, Boolean active) {
        this.id = id;
        this.medicineName = medicineName;
        this.brandName = brandName;
        this.composition = composition;
        this.strength = strength;
        this.category = category;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.requiresPrescription = requiresPrescription;
        this.description = description;
        this.manufacturer = manufacturer;
        this.expiryDate = expiryDate;
        this.active = active;
        this.inStock = stockQuantity != null && stockQuantity > 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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
        this.inStock = stockQuantity != null && stockQuantity > 0;
    }

    public Boolean getRequiresPrescription() {
        return requiresPrescription;
    }

    public void setRequiresPrescription(Boolean requiresPrescription) {
        this.requiresPrescription = requiresPrescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getInStock() {
        return inStock != null ? inStock : (stockQuantity != null && stockQuantity > 0);
    }

    public void setInStock(Boolean inStock) {
        this.inStock = inStock;
    }

    @Override
    public String toString() {
        return "MedicineDTO{" +
                "id=" + id +
                ", medicineName='" + medicineName + '\'' +
                ", brandName='" + brandName + '\'' +
                ", composition='" + composition + '\'' +
                ", strength='" + strength + '\'' +
                ", price=" + price +
                ", stockQuantity=" + stockQuantity +
                ", inStock=" + getInStock() +
                '}';
    }
}
