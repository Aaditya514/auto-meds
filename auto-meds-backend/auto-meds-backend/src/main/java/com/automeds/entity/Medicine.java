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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_medicines")
    @SequenceGenerator(name = "seq_medicines", sequenceName = "SEQ_MEDICINES", allocationSize = 1)
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

    // Maps this field to a database table column
    @Column(nullable = false)
    private Integer active; // 1 = true, 0 = false

    public Medicine() {
        this.stockQuantity = 0;
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

    public Integer getActive() {
        return active;
    }

    public void setActive(Integer active) {
        this.active = active;
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
