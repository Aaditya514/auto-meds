package com.automeds.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: Order
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class Order {

    // Primary Key identifier field
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_orders")
    @SequenceGenerator(name = "seq_orders", sequenceName = "SEQ_ORDERS", allocationSize = 1)
    private Long id;

    // Defines a Many-to-One relational database mapping
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    // Defines a Many-to-One relational database mapping
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    // Maps this field to a database table column
    @Column(name = "order_date", updatable = false)
    private LocalDateTime orderDate;

    // Maps this field to a database table column
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    // Maps this field to a database table column
    @Column(name = "delivery_address", nullable = false, length = 500)
    private String deliveryAddress;

    // Maps this field to a database table column
    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // CASH_ON_DELIVERY, MOCK_ONLINE

    // Maps this field to a database table column
    @Column(name = "payment_status", length = 30)
    private String paymentStatus; // PENDING, PAID, FAILED

    // Maps this field to a database table column
    @Column(name = "order_status", length = 30)
    private String orderStatus; // PENDING, APPROVED, PACKED, DISPATCHED, OUT_FOR_DELIVERY, DELIVERED, CANCELLED, REJECTED

    // Maps this field to a database table column
    @Column(name = "order_type", length = 30)
    private String orderType; // ONE_TIME, SUBSCRIPTION_REFILL

    // Maps this field to a database table column
    @Column(name = "expected_delivery_date")
    private LocalDateTime expectedDeliveryDate;

    private static final String PENDING = "PENDING";
    private static final String ONE_TIME = "ONE_TIME";

    // Defines a One-to-Many relational database mapping
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    public Order() {
        this.orderDate = LocalDateTime.now();
        this.orderStatus = PENDING;
        this.paymentStatus = PENDING;
        this.paymentMethod = "CASH_ON_DELIVERY";
        this.orderType = ONE_TIME;
    }

    @SuppressWarnings("java:S107")
    public Order(Long id, User patient, Subscription subscription, LocalDateTime orderDate, BigDecimal totalAmount, String deliveryAddress, String paymentMethod, String paymentStatus, String orderStatus, String orderType, LocalDateTime expectedDeliveryDate) {
        this.id = id;
        this.patient = patient;
        this.subscription = subscription;
        this.orderDate = orderDate != null ? orderDate : LocalDateTime.now();
        this.totalAmount = totalAmount;
        this.deliveryAddress = deliveryAddress;
        this.paymentMethod = paymentMethod != null ? paymentMethod : "CASH_ON_DELIVERY";
        this.paymentStatus = paymentStatus != null ? paymentStatus : PENDING;
        this.orderStatus = orderStatus != null ? orderStatus : PENDING;
        this.orderType = orderType != null ? orderType : ONE_TIME;
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    @PrePersist
    protected void onCreate() {
        if (this.orderDate == null) {
            this.orderDate = LocalDateTime.now();
        }
        if (this.orderStatus == null) {
            this.orderStatus = "PENDING";
        }
        if (this.paymentStatus == null) {
            this.paymentStatus = "PENDING";
        }
        if (this.orderType == null) {
            this.orderType = "ONE_TIME";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getPatient() {
        return patient;
    }

    public void setPatient(User patient) {
        this.patient = patient;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public LocalDateTime getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(LocalDateTime expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", patientId=" + (patient != null ? patient.getId() : null) +
                ", totalAmount=" + totalAmount +
                ", orderStatus='" + orderStatus + '\'' +
                ", orderType='" + orderType + '\'' +
                '}';
    }
}
