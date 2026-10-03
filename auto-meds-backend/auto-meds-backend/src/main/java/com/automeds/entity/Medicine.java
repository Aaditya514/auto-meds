package com.automeds.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "medicines")
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: Medicine
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class Medicine {

    // Primary Key identifier field
    @Id
    // Use IDENTITY strategy so PostgreSQL auto-increments via BIGSERIAL (no separate sequence objects needed)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Maps this field to a database table column
    @Column(name = "medicine_name", nullable = false, length = 150)
    private String medicineName;

    // Maps this field to a database table column
    @Column(name = "brand_name", nullable = false, length = 100)
    private String brandName;

    // Maps this field to a database table column
    @Column(nullable = false, length = 150)
    private String composition;

    // Maps this field to a database table column
    @Column(nullable = false, length = 50)
    private String strength;

    // Maps this field to a database table column
    @Column(length = 50)
    private String category;

    // Maps this field to a database table column
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // Maps this field to a database table column
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    // Quantity reserved/soft-locked for ongoing subscriptions
    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity = 0;

    // Safety reorder threshold triggering replenishment alerts
    @Column(name = "reorder_threshold", nullable = false)
    private Integer reorderThreshold = 10;

    // Suggested bulk pack reorder size from distributor
    @Column(name = "suggested_reorder_pack_size", nullable = false)
    private Integer suggestedReorderPackSize = 50;

    // Maps this field to a database table column
    @Column(name = "requires_prescription", nullable = false)
    private Integer requiresPrescription; // 1 = true, 0 = false

    // Maps this field to a database table column
    @Column(columnDefinition = "TEXT")
    private String description;

    // Maps this field to a database table column
    @Column(length = 100)
    private String manufacturer;

    // Maps this field to a database table column
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    // Manufacturer batch number for traceability and recall management
    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    // Maps this field to a database table column
    @Column(nullable = false)
    private Integer active; // 1 = true, 0 = false

    // Maps symptom keywords for patient ailment discovery
    @Column(length = 255)
    private String symptoms;

    public Medicine() {
        this.stockQuantity = 0;
        this.reservedQuantity = 0;
        this.reorderThreshold = 10;
        this.suggestedReorderPackSize = 50;
        this.requiresPrescription = 0;
        this.active = 1;
    }

    public Medicine(Long id, String medicineName, String brandName, String composition, String strength, String category, BigDecimal price, Integer stockQuantity, Integer requiresPrescription, String description, String manufacturer, LocalDateTime expiryDate, Integer active) {
        this.id = id;
        this.medicineName = medicineName;
        this.brandName = brandName;
        this.composition = composition;
        this.strength = strength;
        this.category = category;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.requiresPrescription = requiresPrescription != null ? requiresPrescription : 0;
        this.description = description;
        this.manufacturer = manufacturer;
        this.expiryDate = expiryDate;
        this.active = active != null ? active : 1;
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
    }

    public Integer getReservedQuantity() {
        return reservedQuantity != null ? reservedQuantity : 0;
    }

    public void setReservedQuantity(Integer reservedQuantity) {
        this.reservedQuantity = reservedQuantity != null ? reservedQuantity : 0;
    }

    public Integer getAvailableQuantity() {
        int stock = this.stockQuantity != null ? this.stockQuantity : 0;
        int reserved = this.reservedQuantity != null ? this.reservedQuantity : 0;
        return Math.max(0, stock - reserved);
    }

    public Integer getReorderThreshold() {
        return reorderThreshold != null ? reorderThreshold : 10;
    }

    public void setReorderThreshold(Integer reorderThreshold) {
        this.reorderThreshold = reorderThreshold != null ? reorderThreshold : 10;
    }

    public Integer getSuggestedReorderPackSize() {
        return suggestedReorderPackSize != null ? suggestedReorderPackSize : 50;
    }

    public void setSuggestedReorderPackSize(Integer suggestedReorderPackSize) {
        this.suggestedReorderPackSize = suggestedReorderPackSize != null ? suggestedReorderPackSize : 50;
    }

    public Integer getRequiresPrescription() {
        return requiresPrescription;
    }

    public void setRequiresPrescription(Integer requiresPrescription) {
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

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public Integer getActive() {
        return active;
    }

    public void setActive(Integer active) {
        this.active = active;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    @Override
    public String toString() {
        return "Medicine{" +
                "id=" + id +
                ", medicineName='" + medicineName + '\'' +
                ", brandName='" + brandName + '\'' +
                ", composition='" + composition + '\'' +
                ", strength='" + strength + '\'' +
                ", price=" + price +
                ", stockQuantity=" + stockQuantity +
                ", active=" + active +
                '}';
    }
}
