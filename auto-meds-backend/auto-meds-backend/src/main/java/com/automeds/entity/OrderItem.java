package com.automeds.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: OrderItem
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class OrderItem {

    // Primary Key identifier field
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_order_items")
    @SequenceGenerator(name = "seq_order_items", sequenceName = "SEQ_ORDER_ITEMS", allocationSize = 1)
    private Long id;

    // Defines a Many-to-One relational database mapping
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Defines a Many-to-One relational database mapping
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    // Maps this field to a database table column
    @Column(nullable = false)
    private Integer quantity;

    // Maps this field to a database table column
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // Maps this field to a database table column
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    public OrderItem() {
    }

    public OrderItem(Long id, Order order, Medicine medicine, Integer quantity, BigDecimal price, BigDecimal subtotal) {
        this.id = id;
        this.order = order;
        this.medicine = medicine;
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

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
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
        return "OrderItem{" +
                "id=" + id +
                ", medicineId=" + (medicine != null ? medicine.getId() : null) +
                ", quantity=" + quantity +
                ", subtotal=" + subtotal +
                '}';
    }
}
