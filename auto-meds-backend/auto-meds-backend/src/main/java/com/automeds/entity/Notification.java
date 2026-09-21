package com.automeds.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: Notification
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class Notification {

    // Primary Key identifier field
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_notifications")
    @SequenceGenerator(name = "seq_notifications", sequenceName = "SEQ_NOTIFICATIONS", allocationSize = 1)
    private Long id;

    // Defines a Many-to-One relational database mapping
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Maps this field to a database table column
    @Column(nullable = false, length = 255)
    private String title;

    // Maps this field to a database table column
    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    // Maps this field to a database table column
    @Column(length = 30)
    private String type; // INFO, SUCCESS, WARNING, DANGER

    // Maps this field to a database table column
    @Column(name = "is_read", nullable = false)
    private Integer isRead; // 0 = unread, 1 = read

    // Maps this field to a database table column
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Notification() {
        this.createdAt = LocalDateTime.now();
        this.isRead = 0;
        this.type = "INFO";
    }

    public Notification(Long id, User user, String title, String message, String type, Integer isRead) {
        this.id = id;
        this.user = user;
        this.title = title;
        this.message = message;
        this.type = type != null ? type : "INFO";
        this.isRead = isRead != null ? isRead : 0;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.isRead == null) {
            this.isRead = 0;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getIsRead() {
        return isRead;
    }

    public void setIsRead(Integer isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", title='" + title + '\'' +
                ", isRead=" + isRead +
                '}';
    }
}
