package com.automeds.dto;

import java.math.BigDecimal;

/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: OrderItemDTO
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class OrderItemDTO {

    private Long id;
    private Long medicineId;
    private String medicineName;
    private String brandName;
    private String composition;
    private String strength;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;

    public OrderItemDTO() {
    }

    @SuppressWarnings("java:S107")
    public OrderItemDTO(Long id, Long medicineId, String medicineName, String brandName, String composition, String strength, Integer quantity, BigDecimal price, BigDecimal subtotal) {
        this.id = id;
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.brandName = brandName;
        this.composition = composition;
        this.strength = strength;
        this.quantity = quantity;
        this.price = price;
        this.subtotal = subtotal;
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

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    @Override
    public String toString() {
        return "OrderItemDTO{" +
                "id=" + id +
                ", medicineName='" + medicineName + '\'' +
                ", quantity=" + quantity +
                ", subtotal=" + subtotal +
                '}';
    }
}
