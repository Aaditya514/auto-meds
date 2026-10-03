package com.automeds.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * CaregiverAccess Entity
 *
 * Models a trust-delegation relationship between a Patient (the principal) and a
 * Caregiver (the proxy). A caregiver can be a family member or trusted contact who
 * receives notifications and can authorize payments on the patient's behalf.
 *
 * Permissions are scoped — a caregiver may be allowed to:
 *   - VIEW_ONLY: read subscriptions and order history
 *   - NOTIFICATIONS: receive refill alerts and prescription renewal reminders
 *   - PAY_ON_BEHALF: initiate payment for a pre-approved subscription order
 *   - FULL_PROXY: all of the above (e.g., elderly parent scenario)
 */
@Entity
@Table(name = "caregiver_access",
        uniqueConstraints = @UniqueConstraint(columnNames = {"patient_id", "caregiver_id"}))
public class CaregiverAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The patient who is granting proxy access.
     * This is the "principal" — the person whose medications are managed.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    /**
     * The caregiver (family member / proxy) being granted access.
     * Must be a registered user in the system.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caregiver_id", nullable = false)
    private User caregiver;

    /**
     * Comma-separated permission flags:
     * VIEW_ONLY, NOTIFICATIONS, PAY_ON_BEHALF, FULL_PROXY
     */
    @Column(name = "permissions", nullable = false, length = 100)
    private String permissions = "NOTIFICATIONS,PAY_ON_BEHALF";

    /**
     * Status lifecycle: PENDING → ACTIVE → REVOKED
     * PENDING means an invitation email has been sent but not yet accepted.
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";

    /**
     * Short human-readable label (e.g. "Mom", "My Son Rohan") for UI display.
     */
    @Column(name = "relationship_label", length = 50)
    private String relationshipLabel;

    /**
     * Optional: Caregiver phone number for WhatsApp-style SMS command notifications.
     * Can differ from the caregiver user's account phone (e.g., a different device).
     */
    @Column(name = "notify_phone", length = 20)
    private String notifyPhone;

    /** Timestamp when the patient first initiated the invitation. */
    @Column(name = "invited_at", updatable = false)
    private LocalDateTime invitedAt;

    /** Timestamp when the caregiver accepted the invitation. Null if still PENDING. */
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    /** Timestamp when access was revoked. Null if still ACTIVE. */
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @PrePersist
    protected void onCreate() {
        this.invitedAt = LocalDateTime.now();
    }

    // ── Constructors ──────────────────────────────────────────────────────────

    public CaregiverAccess() {}

    public CaregiverAccess(User patient, User caregiver, String relationshipLabel, String permissions) {
        this.patient = patient;
        this.caregiver = caregiver;
        this.relationshipLabel = relationshipLabel;
        this.permissions = permissions;
        this.status = "PENDING";
        this.invitedAt = LocalDateTime.now();
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getPatient() { return patient; }
    public void setPatient(User patient) { this.patient = patient; }

    public User getCaregiver() { return caregiver; }
    public void setCaregiver(User caregiver) { this.caregiver = caregiver; }

    public String getPermissions() { return permissions; }
    public void setPermissions(String permissions) { this.permissions = permissions; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRelationshipLabel() { return relationshipLabel; }
    public void setRelationshipLabel(String relationshipLabel) { this.relationshipLabel = relationshipLabel; }

    public String getNotifyPhone() { return notifyPhone; }
    public void setNotifyPhone(String notifyPhone) { this.notifyPhone = notifyPhone; }

    public LocalDateTime getInvitedAt() { return invitedAt; }
    public void setInvitedAt(LocalDateTime invitedAt) { this.invitedAt = invitedAt; }

    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; }

    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }

    // ── Convenience ───────────────────────────────────────────────────────────

    public boolean hasPermission(String perm) {
        if (permissions == null) return false;
        for (String p : permissions.split(",")) {
            if (p.trim().equalsIgnoreCase(perm) || p.trim().equalsIgnoreCase("FULL_PROXY")) {
                return true;
            }
        }
        return false;
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }
}
