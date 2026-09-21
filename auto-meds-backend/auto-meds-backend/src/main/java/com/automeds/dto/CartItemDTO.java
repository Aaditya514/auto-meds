package com.automeds.dto;

import java.math.BigDecimal;

/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: CartItemDTO
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class CartItemDTO {

    private Long id;
    private Long medicineId;
    private String medicineName;
    private String brandName;
    private String composition;
    private String strength;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
    private Boolean inStock;

    public CartItemDTO() {
    }

    @SuppressWarnings("java:S107")
    public CartItemDTO(Long id, Long medicineId, String medicineName, String brandName, String composition, String strength, BigDecimal price, Integer quantity, BigDecimal subtotal, Boolean inStock) {
        this.id = id;
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.brandName = brandName;
        this.composition = composition;
        this.strength = strength;
        this.price = price;
        this.quantity = quantity;
        this.subtotal = subtotal;
        this.inStock = inStock;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public Boolean getInStock() {
        return inStock;
    }

    public void setInStock(Boolean inStock) {
        this.inStock = inStock;
    }

    @Override
    public String toString() {
        return "CartItemDTO{" +
                "id=" + id +
                ", medicineId=" + medicineId +
                ", medicineName='" + medicineName + '\'' +
                ", quantity=" + quantity +
                ", subtotal=" + subtotal +
                '}';
    }
}
