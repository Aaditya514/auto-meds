package com.automeds.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: CartDTO
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class CartDTO {

    private Long id;
    private Long patientId;
    private List<CartItemDTO> items = new ArrayList<>();
    private Integer totalItems = 0;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal deliveryCharge = new BigDecimal("40.00");
    private BigDecimal totalAmount = BigDecimal.ZERO;

    public CartDTO() {
    }

    public CartDTO(Long id, Long patientId, List<CartItemDTO> items, Integer totalItems, BigDecimal subtotal, BigDecimal deliveryCharge, BigDecimal totalAmount) {
        this.id = id;
        this.patientId = patientId;
        this.items = items != null ? items : new ArrayList<>();
        this.totalItems = totalItems != null ? totalItems : 0;
        this.subtotal = subtotal != null ? subtotal : BigDecimal.ZERO;
        this.deliveryCharge = deliveryCharge != null ? deliveryCharge : new BigDecimal("40.00");
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public List<CartItemDTO> getItems() {
        return items;
    }

    public void setItems(List<CartItemDTO> items) {
        this.items = items;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDeliveryCharge() {
        return deliveryCharge;
    }

    public void setDeliveryCharge(BigDecimal deliveryCharge) {
        this.deliveryCharge = deliveryCharge;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    @Override
    public String toString() {
        return "CartDTO{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", totalItems=" + totalItems +
                ", subtotal=" + subtotal +
                ", totalAmount=" + totalAmount +
                '}';
    }
}
