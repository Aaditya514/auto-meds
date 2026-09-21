package com.automeds.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Mark this class as a database Entity mapped to a database table
@Entity
// Specify the database table name where this entity data is stored
@Table(name = "users")
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: User
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class User {

    // Define the Primary Key field
    // Primary Key identifier field
    @Id
    // Configure database sequence generation (Oracle specific SEQUENCE generator)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_users")
    @SequenceGenerator(name = "seq_users", sequenceName = "SEQ_USERS", allocationSize = 1)
    private Long id;

    // Full name of the user, maximum 100 characters, cannot be null in database
    // Maps this field to a database table column
    @Column(nullable = false, length = 100)
    private String name;

    // Unique email address used for login, cannot be null, must be unique across all users
    // Maps this field to a database table column
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // Encrypted password hash, stored up to 255 characters
    // Maps this field to a database table column
    @Column(nullable = false, length = 255)
    private String password;

    // User privilege role (e.g. "PATIENT" or "ADMIN")
    // Maps this field to a database table column
    @Column(nullable = false, length = 20)
    private String role; // "PATIENT", "ADMIN"

    // Optional phone number field
    // Maps this field to a database table column
    @Column(length = 20)
    private String phone;

    // Optional street address details
    // Maps this field to a database table column
    @Column(length = 255)
    private String address;

    // Optional city name
    // Maps this field to a database table column
    @Column(length = 100)
    private String city;

    // Optional state name
    // Maps this field to a database table column
    @Column(length = 100)
    private String state;

    // Optional 6-digit pincode area code
    // Maps this field to a database table column
    @Column(length = 20)
    private String pincode;

    // Timestamp when this user account was created, cannot be updated after insertion
    // Maps this field to a database table column
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Default Constructor (no-args) required by JPA Hibernate
    public User() {
        this.createdAt = LocalDateTime.now();
        this.role = "PATIENT";
    }

    // Parametrized Constructor used for instantiating users with fields
    @SuppressWarnings("java:S107")
    public User(Long id, String name, String email, String password, String role, String phone, String address, String city, String state, String pincode) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role != null ? role : "PATIENT";
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
        this.createdAt = LocalDateTime.now();
    }

    // JPA lifecycle callback that sets the createdAt field right before inserting the record into Oracle DB
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", phone='" + phone + '\'' +
                ", city='" + city + '\'' +
                '}';
    }
}
