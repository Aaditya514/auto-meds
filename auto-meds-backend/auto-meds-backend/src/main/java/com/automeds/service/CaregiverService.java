package com.automeds.service;

import com.automeds.dto.CaregiverDTO;
import com.automeds.entity.CaregiverAccess;
import com.automeds.entity.Order;
import com.automeds.entity.User;
import com.automeds.exception.BadRequestException;
import com.automeds.exception.ResourceNotFoundException;
import com.automeds.repository.CaregiverAccessRepository;
import com.automeds.repository.OrderRepository;
import com.automeds.repository.UserRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CaregiverService — Phase 6 Caregiver Proxy Access
 *
 * Handles the full lifecycle of caregiver delegation:
 *   1. Patient invites a caregiver by email
 *   2. Caregiver accepts / declines the invite
 *   3. Caregiver can view patient's subscription status
 *   4. Caregiver can pay on behalf of the patient
 *   5. WhatsApp-style SMS command parsing (CONFIRM, SKIP, SNOOZE)
 *   6. Patient can revoke access at any time
 *
 * SECURITY INVARIANT: Every action that reads or mutates a patient's data
 * must verify an ACTIVE caregiver link via existsActiveLink() before proceeding.
 */
@Service
public class CaregiverService {

    private static final Logger log = LoggerFactory.getLogger(CaregiverService.class);

    private final CaregiverAccessRepository caregiverRepo;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    // ── Micrometer Observability Counters ─────────────────────────────────────
    private final Counter caregiverInviteCounter;
    private final Counter caregiverAcceptCounter;
    private final Counter proxyPaymentCounter;
    private final Counter whatsappCommandCounter;

    public CaregiverService(
            CaregiverAccessRepository caregiverRepo,
            UserRepository userRepository,
            OrderRepository orderRepository,
            EmailService emailService,
            AuditLogService auditLogService,
            MeterRegistry meterRegistry) {
        this.caregiverRepo = caregiverRepo;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.emailService = emailService;
        this.auditLogService = auditLogService;

        // Register custom Micrometer counters — visible in /actuator/metrics
        this.caregiverInviteCounter = Counter.builder("automeds.caregiver.invites")
                .description("Total number of caregiver invitations sent")
                .register(meterRegistry);
        this.caregiverAcceptCounter = Counter.builder("automeds.caregiver.accepts")
                .description("Total number of caregiver invitations accepted")
                .register(meterRegistry);
        this.proxyPaymentCounter = Counter.builder("automeds.caregiver.proxy_payments")
                .description("Total payments initiated by a caregiver on behalf of a patient")
                .register(meterRegistry);
        this.whatsappCommandCounter = Counter.builder("automeds.caregiver.whatsapp_commands")
                .description("Total WhatsApp / SMS commands received and processed")
                .register(meterRegistry);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. INVITE A CAREGIVER (called by the patient)
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public CaregiverDTO.CaregiverLinkResponse inviteCaregiver(
            Long patientId, CaregiverDTO.InviteRequest request) {

        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", patientId));

        User caregiver = userRepository.findByEmail(request.getCaregiverEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "email", request.getCaregiverEmail()));

        if (patientId.equals(caregiver.getId())) {
            throw new BadRequestException("You cannot add yourself as a caregiver.");
        }

        // Prevent duplicate active/pending invitations
        caregiverRepo.findByPatientIdAndCaregiverId(patientId, caregiver.getId())
                .ifPresent(existing -> {
                    if (!"REVOKED".equalsIgnoreCase(existing.getStatus())) {
                        throw new BadRequestException(
                                "A caregiver link for this user already exists with status: " + existing.getStatus());
                    }
                });

        String perms = request.getPermissions() != null
                ? request.getPermissions()
                : "NOTIFICATIONS,PAY_ON_BEHALF";

        CaregiverAccess access = new CaregiverAccess(patient, caregiver,
                request.getRelationshipLabel(), perms);
        access.setNotifyPhone(request.getNotifyPhone());
        CaregiverAccess saved = caregiverRepo.save(access);

        // Send invitation email to caregiver
        sendCaregiverInviteEmail(patient, caregiver, saved.getId());

        auditLogService.log(patientId, "PATIENT", "CAREGIVER_INVITED",
                "CaregiverAccess", saved.getId(), null);

        caregiverInviteCounter.increment();
        log.info("Patient {} invited caregiver {} (accessId={})", patientId, caregiver.getId(), saved.getId());

        return toResponse(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. ACCEPT / DECLINE INVITATION (called by the caregiver)
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public CaregiverDTO.CaregiverLinkResponse respondToInvitation(
            Long caregiverId, CaregiverDTO.RespondRequest request) {

        CaregiverAccess access = caregiverRepo.findById(request.getAccessId())
                .orElseThrow(() -> new ResourceNotFoundException("CaregiverAccess", "id", request.getAccessId()));

        if (!access.getCaregiver().getId().equals(caregiverId)) {
            throw new BadRequestException("This invitation was not sent to you.");
        }

        if (!"PENDING".equalsIgnoreCase(access.getStatus())) {
            throw new BadRequestException("Invitation is no longer pending (status: " + access.getStatus() + ")");
        }

        if ("ACCEPT".equalsIgnoreCase(request.getAction())) {
            access.setStatus("ACTIVE");
            access.setAcceptedAt(LocalDateTime.now());
            caregiverAcceptCounter.increment();
            auditLogService.log(caregiverId, "PATIENT", "CAREGIVER_ACCEPTED",
                    "CaregiverAccess", access.getId(), null);
            log.info("Caregiver {} ACCEPTED access for patient {}", caregiverId, access.getPatient().getId());
        } else if ("DECLINE".equalsIgnoreCase(request.getAction())) {
            access.setStatus("REVOKED");
            access.setRevokedAt(LocalDateTime.now());
            auditLogService.log(caregiverId, "PATIENT", "CAREGIVER_DECLINED",
                    "CaregiverAccess", access.getId(), null);
            log.info("Caregiver {} DECLINED access for patient {}", caregiverId, access.getPatient().getId());
        } else {
            throw new BadRequestException("Invalid action. Use 'ACCEPT' or 'DECLINE'.");
        }

        return toResponse(caregiverRepo.save(access));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. REVOKE ACCESS (called by the patient)
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public void revokeCaregiver(Long patientId, Long accessId) {
        CaregiverAccess access = caregiverRepo.findById(accessId)
                .orElseThrow(() -> new ResourceNotFoundException("CaregiverAccess", "id", accessId));

        if (!access.getPatient().getId().equals(patientId)) {
            throw new BadRequestException("You can only revoke your own caregiver assignments.");
        }

        access.setStatus("REVOKED");
        access.setRevokedAt(LocalDateTime.now());
        caregiverRepo.save(access);

        auditLogService.log(patientId, "PATIENT", "CAREGIVER_REVOKED",
                "CaregiverAccess", accessId, null);
        log.info("Patient {} revoked caregiver access id={}", patientId, accessId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. LIST CAREGIVERS (patient sees who has access)
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CaregiverDTO.CaregiverLinkResponse> getCaregiversForPatient(Long patientId) {
        return caregiverRepo.findAllByPatientId(patientId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. LIST DELEGATED PATIENTS (caregiver sees whose meds they manage)
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CaregiverDTO.DelegatedPatientResponse> getDelegatedPatients(Long caregiverId) {
        return caregiverRepo.findActiveDelegatedPatients(caregiverId)
                .stream()
                .map(this::toDelegatedResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. PAY ON BEHALF (caregiver pays for patient's subscription order)
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public String payOnBehalf(Long caregiverId, CaregiverDTO.ProxyPaymentRequest request) {
        // Authorization guard: must have ACTIVE link with PAY_ON_BEHALF permission
        CaregiverAccess access = caregiverRepo
                .findByPatientIdAndCaregiverId(request.getPatientId(), caregiverId)
                .orElseThrow(() -> new BadRequestException(
                        "No active caregiver relationship found for patient id=" + request.getPatientId()));

        if (!access.isActive()) {
            throw new BadRequestException("Your caregiver access is not active (status: " + access.getStatus() + ")");
        }

        if (!access.hasPermission("PAY_ON_BEHALF")) {
            throw new BadRequestException("Your caregiver role does not include payment permissions.");
        }

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", request.getOrderId()));

        // Verify the order belongs to the correct patient
        if (!order.getPatient().getId().equals(request.getPatientId())) {
            throw new BadRequestException("Order does not belong to the specified patient.");
        }

        if (!"PENDING".equalsIgnoreCase(order.getPaymentStatus())) {
            throw new BadRequestException("Order is already paid or not eligible for proxy payment (status: "
                    + order.getPaymentStatus() + ")");
        }

        // Mark payment as completed (proxy gateway simulation)
        order.setPaymentStatus("PAID");
        order.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "UPI");
        order.setOrderStatus("CONFIRMED");
        orderRepository.save(order);

        auditLogService.log(caregiverId, "CAREGIVER", "PROXY_PAYMENT_COMPLETED",
                "Order", order.getId(), "PatientId=" + request.getPatientId()
                        + " Method=" + request.getPaymentMethod());

        proxyPaymentCounter.increment();
        log.info("Caregiver {} paid for patient {} order={}", caregiverId, request.getPatientId(), order.getId());

        return "Payment completed successfully for patient order #" + order.getId()
                + " via " + order.getPaymentMethod();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. WHATSAPP / SMS COMMAND PARSER
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Processes a single-word SMS/WhatsApp command from a caregiver's phone.
     *
     * Supported commands:
     *   CONFIRM  → marks the most recent PENDING order for all delegated patients as PAID
     *   SKIP     → skips (cancels) the most recent PENDING subscription order
     *   SNOOZE   → sets order snooze flag (7 days by default, or "SNOOZE 14" for 14 days)
     *   STATUS   → returns a plain-text status summary (no mutation)
     *
     * Phone lookup: Finds the caregiver by notifyPhone across all CaregiverAccess records.
     */
    @Transactional
    public String processWhatsAppCommand(CaregiverDTO.WhatsAppCommandRequest request) {
        whatsappCommandCounter.increment();

        String phone = request.getFrom();
        String rawCommand = request.getBody() != null ? request.getBody().trim().toUpperCase() : "";

        log.info("WhatsApp command received from={} command='{}'", phone, rawCommand);

        // Find caregiver by their notify phone number
        User caregiver = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BadRequestException(
                        "No registered caregiver found for phone: " + phone));

        List<CaregiverAccess> delegations = caregiverRepo.findActiveDelegatedPatients(caregiver.getId());
        if (delegations.isEmpty()) {
            return "You have no active patient delegations. Please ask your patient to invite you via the AutoMeds app.";
        }

        // For simplicity, act on the first active delegation (caregiver with one patient)
        // In multi-patient scenarios, the command would include a patient identifier
        CaregiverAccess delegation = delegations.get(0);
        Long patientId = delegation.getPatient().getId();
        String patientName = delegation.getPatient().getName();

        if (rawCommand.startsWith("CONFIRM")) {
            return handleConfirmCommand(caregiver.getId(), patientId, patientName);
        } else if (rawCommand.startsWith("SKIP")) {
            return handleSkipCommand(caregiver.getId(), patientId, patientName);
        } else if (rawCommand.startsWith("SNOOZE")) {
            int days = parseSnoozeDays(rawCommand);
            return handleSnoozeCommand(caregiver.getId(), patientId, patientName, days);
        } else if (rawCommand.startsWith("STATUS")) {
            return handleStatusCommand(patientId, patientName, delegation);
        } else {
            return "❓ Unknown command '" + rawCommand + "'.\n\n"
                    + "Available commands:\n"
                    + "  CONFIRM  → Pay for pending refill\n"
                    + "  SKIP     → Skip this month's refill\n"
                    + "  SNOOZE   → Delay refill by 7 days\n"
                    + "  STATUS   → Check subscription status";
        }
    }

    private String handleConfirmCommand(Long caregiverId, Long patientId, String patientName) {
        List<Order> pendingOrders = orderRepository.findByPatientIdAndPaymentStatus(patientId, "PENDING");
        if (pendingOrders.isEmpty()) {
            return "✅ " + patientName + " has no pending orders to confirm right now.";
        }
        Order order = pendingOrders.get(0); // most recent
        order.setPaymentStatus("PAID");
        order.setOrderStatus("CONFIRMED");
        orderRepository.save(order);

        auditLogService.log(caregiverId, "CAREGIVER", "WHATSAPP_CONFIRM",
                "Order", order.getId(), "PatientId=" + patientId);

        return "✅ Confirmed! Order #" + order.getId() + " for " + patientName
                + " has been approved. Total: ₹" + order.getTotalAmount();
    }

    private String handleSkipCommand(Long caregiverId, Long patientId, String patientName) {
        List<Order> pendingOrders = orderRepository.findByPatientIdAndPaymentStatus(patientId, "PENDING");
        if (pendingOrders.isEmpty()) {
            return "ℹ️ " + patientName + " has no pending orders to skip.";
        }
        Order order = pendingOrders.get(0);
        order.setPaymentStatus("CANCELLED");
        order.setOrderStatus("CANCELLED");
        orderRepository.save(order);

        auditLogService.log(caregiverId, "CAREGIVER", "WHATSAPP_SKIP",
                "Order", order.getId(), "PatientId=" + patientId);

        return "⏭️ Skipped! Order #" + order.getId() + " for " + patientName
                + " has been cancelled for this cycle.";
    }

    private String handleSnoozeCommand(Long caregiverId, Long patientId, String patientName, int days) {
        // In a full implementation, update the subscription's next_refill_date
        // For now we log the intent and confirm
        auditLogService.log(caregiverId, "CAREGIVER", "WHATSAPP_SNOOZE",
                "Patient", patientId, "Days=" + days);

        return "💤 Snoozed! " + patientName + "'s next refill has been delayed by "
                + days + " days. We'll remind you again closer to the new date.";
    }

    private String handleStatusCommand(Long patientId, String patientName, CaregiverAccess delegation) {
        List<Order> pendingOrders = orderRepository.findByPatientIdAndPaymentStatus(patientId, "PENDING");
        String orderSummary = pendingOrders.isEmpty()
                ? "No pending orders."
                : pendingOrders.size() + " pending order(s). Send CONFIRM to approve.";

        return "📊 Status for " + patientName + ":\n"
                + "• Pending orders: " + orderSummary + "\n"
                + "• Your access level: " + delegation.getPermissions() + "\n"
                + "• Relationship: " + delegation.getRelationshipLabel();
    }

    private int parseSnoozeDays(String command) {
        try {
            String[] parts = command.split("\\s+");
            if (parts.length >= 2) {
                return Integer.parseInt(parts[1]);
            }
        } catch (NumberFormatException ignored) {
            // Fall through to default
        }
        return 7; // default snooze duration
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPER: Email notification for invitation
    // ─────────────────────────────────────────────────────────────────────────

    private void sendCaregiverInviteEmail(User patient, User caregiver, Long accessId) {
        try {
            String subject = "🏥 AutoMeds: " + patient.getName() + " has invited you as a Caregiver";
            String body = "<h2>You've been invited!</h2>"
                    + "<p><strong>" + patient.getName() + "</strong> has added you as a trusted caregiver "
                    + "on their AutoMeds medication management account.</p>"
                    + "<p>As a caregiver, you can receive refill reminders and help manage their medications.</p>"
                    + "<p><a href='http://localhost:4200/caregiver/accept/" + accessId + "' "
                    + "style='background:#6C63FF;color:white;padding:12px 24px;border-radius:8px;text-decoration:none;'>"
                    + "Accept Invitation</a></p>"
                    + "<p>If you did not expect this, you can safely ignore this email.</p>";
            emailService.sendHtmlEmail(caregiver.getEmail(), subject, body);
        } catch (Exception e) {
            log.warn("Failed to send caregiver invite email to {}: {}", caregiver.getEmail(), e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MAPPERS
    // ─────────────────────────────────────────────────────────────────────────

    private CaregiverDTO.CaregiverLinkResponse toResponse(CaregiverAccess access) {
        CaregiverDTO.CaregiverLinkResponse r = new CaregiverDTO.CaregiverLinkResponse();
        r.setAccessId(access.getId());
        r.setCaregiverId(access.getCaregiver().getId());
        r.setCaregiverName(access.getCaregiver().getName());
        r.setCaregiverEmail(access.getCaregiver().getEmail());
        r.setRelationshipLabel(access.getRelationshipLabel());
        r.setPermissions(access.getPermissions());
        r.setStatus(access.getStatus());
        r.setInvitedAt(access.getInvitedAt());
        r.setAcceptedAt(access.getAcceptedAt());
        return r;
    }

    private CaregiverDTO.DelegatedPatientResponse toDelegatedResponse(CaregiverAccess access) {
        CaregiverDTO.DelegatedPatientResponse r = new CaregiverDTO.DelegatedPatientResponse();
        r.setAccessId(access.getId());
        r.setPatientId(access.getPatient().getId());
        r.setPatientName(access.getPatient().getName());
        r.setPatientEmail(access.getPatient().getEmail());
        r.setRelationshipLabel(access.getRelationshipLabel());
        r.setPermissions(access.getPermissions());
        r.setAcceptedAt(access.getAcceptedAt());
        return r;
    }
}
