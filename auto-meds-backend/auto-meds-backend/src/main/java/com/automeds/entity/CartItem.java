package com.automeds.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "cart_items")
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: CartItem
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class CartItem {

    // Primary Key identifier field
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_cart_items")
    @SequenceGenerator(name = "seq_cart_items", sequenceName = "SEQ_CART_ITEMS", allocationSize = 1)
    private Long id;

    // Defines a Many-to-One relational database mapping
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    // Defines a Many-to-One relational database mapping
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    // Maps this field to a database table column
    @Column(nullable = false)
    private Integer quantity;

    // Maps this field to a database table column
    @Column(name = "price_at_addition", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtAddition;

    public CartItem() {
    }

    public CartItem(Long id, Cart cart, Medicine medicine, Integer quantity, BigDecimal priceAtAddition) {
        this.id = id;
        this.cart = cart;
        this.medicine = medicine;
        this.quantity = quantity;
        this.priceAtAddition = priceAtAddition;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
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

    public BigDecimal getPriceAtAddition() {
        return priceAtAddition;
    }

    public void setPriceAtAddition(BigDecimal priceAtAddition) {
        this.priceAtAddition = priceAtAddition;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "id=" + id +
                ", medicineId=" + (medicine != null ? medicine.getId() : null) +
                ", quantity=" + quantity +
                ", priceAtAddition=" + priceAtAddition +
                '}';
    }
}
