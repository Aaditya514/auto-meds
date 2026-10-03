package com.automeds.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Objects for Caregiver Proxy Access feature.
 *
 * All caregiver-related request/response payloads live here as
 * inner static classes to keep them co-located and easy to discover.
 */
public class CaregiverDTO {

    // ── Incoming Request DTOs ─────────────────────────────────────────────────

    /**
     * Payload sent by a patient to invite a new caregiver by email address.
     */
    public static class InviteRequest {
        /** Registered email of the person to invite as a caregiver. */
        private String caregiverEmail;
        /** Human-readable label for this relationship (e.g. "My daughter Priya"). */
        private String relationshipLabel;
        /**
         * Comma-separated permission flags:
         * VIEW_ONLY, NOTIFICATIONS, PAY_ON_BEHALF, FULL_PROXY
         */
        private String permissions;
        /** Optional separate phone for WhatsApp/SMS notifications. */
        private String notifyPhone;

        public String getCaregiverEmail() { return caregiverEmail; }
        public void setCaregiverEmail(String caregiverEmail) { this.caregiverEmail = caregiverEmail; }

        public String getRelationshipLabel() { return relationshipLabel; }
        public void setRelationshipLabel(String relationshipLabel) { this.relationshipLabel = relationshipLabel; }

        public String getPermissions() { return permissions; }
        public void setPermissions(String permissions) { this.permissions = permissions; }

        public String getNotifyPhone() { return notifyPhone; }
        public void setNotifyPhone(String notifyPhone) { this.notifyPhone = notifyPhone; }
    }

    /**
     * Payload sent by a caregiver to accept or decline a pending invitation.
     */
    public static class RespondRequest {
        /** The CaregiverAccess record ID to respond to. */
        private Long accessId;
        /** "ACCEPT" or "DECLINE" */
        private String action;

        public Long getAccessId() { return accessId; }
        public void setAccessId(Long accessId) { this.accessId = accessId; }

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
    }

    /**
     * Payload for a caregiver initiating payment for a patient's pending subscription order.
     */
    public static class ProxyPaymentRequest {
        /** The patient's user ID for whom payment is being made. */
        private Long patientId;
        /** The order/subscription ID to pay for. */
        private Long orderId;
        /** Payment method: "UPI", "CARD", "COD" */
        private String paymentMethod;
        /** UPI ID or card token, etc. (optional based on method) */
        private String paymentReference;

        public Long getPatientId() { return patientId; }
        public void setPatientId(Long patientId) { this.patientId = patientId; }

        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }

        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

        public String getPaymentReference() { return paymentReference; }
        public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
    }

    /**
     * Inbound webhook payload from a WhatsApp / SMS gateway.
     * A caregiver can reply with single-word commands to act on behalf of a patient.
     *
     * Supported commands:
     *   CONFIRM  → approve the pending refill order
     *   SKIP     → skip this month's refill cycle
     *   SNOOZE   → delay refill by 7 days
     *   STATUS   → reply with current subscription status summary
     */
    public static class WhatsAppCommandRequest {
        /** Sender phone number (E.164 format, e.g. "+919876543210"). */
        private String from;
        /** Raw message body (e.g. "CONFIRM" or "SNOOZE 14"). */
        private String body;
        /** Optional: gateway-supplied message SID for idempotency. */
        private String messageSid;

        public String getFrom() { return from; }
        public void setFrom(String from) { this.from = from; }

        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }

        public String getMessageSid() { return messageSid; }
        public void setMessageSid(String messageSid) { this.messageSid = messageSid; }
    }

    // ── Response DTOs ─────────────────────────────────────────────────────────

    /**
     * Response object describing a caregiver link from the patient's perspective.
     */
    public static class CaregiverLinkResponse {
        private Long accessId;
        private Long caregiverId;
        private String caregiverName;
        private String caregiverEmail;
        private String relationshipLabel;
        private String permissions;
        private String status;
        private LocalDateTime invitedAt;
        private LocalDateTime acceptedAt;

        public Long getAccessId() { return accessId; }
        public void setAccessId(Long accessId) { this.accessId = accessId; }

        public Long getCaregiverId() { return caregiverId; }
        public void setCaregiverId(Long caregiverId) { this.caregiverId = caregiverId; }

        public String getCaregiverName() { return caregiverName; }
        public void setCaregiverName(String caregiverName) { this.caregiverName = caregiverName; }

        public String getCaregiverEmail() { return caregiverEmail; }
        public void setCaregiverEmail(String caregiverEmail) { this.caregiverEmail = caregiverEmail; }

        public String getRelationshipLabel() { return relationshipLabel; }
        public void setRelationshipLabel(String relationshipLabel) { this.relationshipLabel = relationshipLabel; }

        public String getPermissions() { return permissions; }
        public void setPermissions(String permissions) { this.permissions = permissions; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public LocalDateTime getInvitedAt() { return invitedAt; }
        public void setInvitedAt(LocalDateTime invitedAt) { this.invitedAt = invitedAt; }

        public LocalDateTime getAcceptedAt() { return acceptedAt; }
        public void setAcceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; }
    }

    /**
     * Response describing a delegated patient from the caregiver's perspective.
     */
    public static class DelegatedPatientResponse {
        private Long accessId;
        private Long patientId;
        private String patientName;
        private String patientEmail;
        private String relationshipLabel;
        private String permissions;
        private LocalDateTime acceptedAt;

        public Long getAccessId() { return accessId; }
        public void setAccessId(Long accessId) { this.accessId = accessId; }

        public Long getPatientId() { return patientId; }
        public void setPatientId(Long patientId) { this.patientId = patientId; }

        public String getPatientName() { return patientName; }
        public void setPatientName(String patientName) { this.patientName = patientName; }

        public String getPatientEmail() { return patientEmail; }
        public void setPatientEmail(String patientEmail) { this.patientEmail = patientEmail; }

        public String getRelationshipLabel() { return relationshipLabel; }
        public void setRelationshipLabel(String relationshipLabel) { this.relationshipLabel = relationshipLabel; }

        public String getPermissions() { return permissions; }
        public void setPermissions(String permissions) { this.permissions = permissions; }

        public LocalDateTime getAcceptedAt() { return acceptedAt; }
        public void setAcceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; }
    }
}
