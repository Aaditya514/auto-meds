package com.automeds.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: Subscription
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class Subscription {

    // Primary Key identifier field
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_subscriptions")
    @SequenceGenerator(name = "seq_subscriptions", sequenceName = "SEQ_SUBSCRIPTIONS", allocationSize = 1)
    private Long id;

    // Defines a Many-to-One relational database mapping
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    // Defines a Many-to-One relational database mapping
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;

    // Defines a Many-to-One relational database mapping
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "prescription_id")
    private Prescription prescription;

    // Maps this field to a database table column
    @Column(nullable = true, length = 50)
    private String dosage; // e.g. "1 tablet/day"

    // Maps this field to a database table column
    @Column(nullable = true, length = 50)
    private String frequency; // e.g. "Daily", "Once Daily"

    // Maps this field to a database table column
    @Column(nullable = true)
    private Integer quantity; // e.g. 30 tablets

    // Maps this field to a database table column
    @Column(name = "start_date")
    private LocalDateTime startDate;

    // Maps this field to a database table column
    @Column(name = "next_refill_date")
    private LocalDateTime nextRefillDate;

    // Maps this field to a database table column
    @Column(name = "next_dispatch_date")
    private LocalDateTime nextDispatchDate;

    // Maps this field to a database table column
    @Column(nullable = false, length = 30)
    private String status; // PENDING, ACTIVE, REJECTED, CANCELLED, EXPIRED, PRESCRIPTION_EXPIRED, CLARIFICATION_REQUIRED, PAUSED

    // Maps this field to a database table column
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Maps this field to a database table column
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private static final String PENDING = "PENDING";

    public Subscription() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = PENDING;
    }

    @SuppressWarnings("java:S107")
    public Subscription(Long id, User patient, Medicine medicine, Prescription prescription, String dosage, String frequency, Integer quantity, LocalDateTime startDate, LocalDateTime nextRefillDate, LocalDateTime nextDispatchDate, String status) {
        this.id = id;
        this.patient = patient;
        this.medicine = medicine;
        this.prescription = prescription;
        this.dosage = dosage;
        this.frequency = frequency;
        this.quantity = quantity;
        this.startDate = startDate;
        this.nextRefillDate = nextRefillDate;
        this.nextDispatchDate = nextDispatchDate;
        this.status = status != null ? status : PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
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

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
    }

    public Prescription getPrescription() {
        return prescription;
    }

    public void setPrescription(Prescription prescription) {
        this.prescription = prescription;
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

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getNextRefillDate() {
        return nextRefillDate;
    }

    public void setNextRefillDate(LocalDateTime nextRefillDate) {
        this.nextRefillDate = nextRefillDate;
    }

    public LocalDateTime getNextDispatchDate() {
        return nextDispatchDate;
    }

    public void setNextDispatchDate(LocalDateTime nextDispatchDate) {
        this.nextDispatchDate = nextDispatchDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "id=" + id +
                ", patientId=" + (patient != null ? patient.getId() : null) +
                ", medicineId=" + (medicine != null ? medicine.getId() : null) +
                ", dosage='" + dosage + '\'' +
                ", frequency='" + frequency + '\'' +
                ", quantity=" + quantity +
                ", status='" + status + '\'' +
                '}';
    }
}
