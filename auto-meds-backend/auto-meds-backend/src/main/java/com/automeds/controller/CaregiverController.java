package com.automeds.controller;

import com.automeds.dto.CaregiverDTO;
import com.automeds.security.UserPrincipal;
import com.automeds.service.CaregiverService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * CaregiverController — Phase 6 REST API
 *
 * Exposes endpoints for the caregiver proxy feature:
 *
 * Patient endpoints:
 *   POST   /api/caregiver/invite            → Invite a caregiver by email
 *   GET    /api/caregiver/my-caregivers     → List all caregivers for logged-in patient
 *   DELETE /api/caregiver/{accessId}/revoke → Revoke a caregiver's access
 *
 * Caregiver endpoints:
 *   POST /api/caregiver/respond             → Accept or decline a pending invitation
 *   GET  /api/caregiver/my-patients         → List delegated patients the caregiver manages
 *   POST /api/caregiver/pay-on-behalf       → Make a payment for a patient's order
 *
 * Webhook endpoint (SMS / WhatsApp gateway):
 *   POST /api/caregiver/whatsapp-command    → Parse CONFIRM/SKIP/SNOOZE/STATUS commands
 *
 * Security: All endpoints require authentication. Ownership verification
 * is enforced in CaregiverService — never from the client-supplied payload.
 */
@RestController
@RequestMapping("/api/caregiver")
public class CaregiverController {

    private final CaregiverService caregiverService;

    public CaregiverController(CaregiverService caregiverService) {
        this.caregiverService = caregiverService;
    }

    // ── Patient Endpoints ─────────────────────────────────────────────────────

    /**
     * POST /api/caregiver/invite
     * Allows a PATIENT to invite a registered user to be their caregiver.
     */
    @PostMapping("/invite")
    public ResponseEntity<CaregiverDTO.CaregiverLinkResponse> inviteCaregiver(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody CaregiverDTO.InviteRequest request) {

        CaregiverDTO.CaregiverLinkResponse response =
                caregiverService.inviteCaregiver(currentUser.getId(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/caregiver/my-caregivers
     * Returns all caregiver relationships (PENDING, ACTIVE, REVOKED) for the logged-in patient.
     */
    @GetMapping("/my-caregivers")
    public ResponseEntity<List<CaregiverDTO.CaregiverLinkResponse>> getMyCaregivers(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        return ResponseEntity.ok(caregiverService.getCaregiversForPatient(currentUser.getId()));
    }

    /**
     * DELETE /api/caregiver/{accessId}/revoke
     * Allows a PATIENT to revoke an existing caregiver's access immediately.
     */
    @DeleteMapping("/{accessId}/revoke")
    public ResponseEntity<Map<String, String>> revokeCaregiver(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long accessId) {

        caregiverService.revokeCaregiver(currentUser.getId(), accessId);
        return ResponseEntity.ok(Map.of(
                "message", "Caregiver access revoked successfully.",
                "accessId", String.valueOf(accessId)
        ));
    }

    // ── Caregiver Endpoints ───────────────────────────────────────────────────

    /**
     * POST /api/caregiver/respond
     * Allows a caregiver (invited user) to ACCEPT or DECLINE a pending invitation.
     */
    @PostMapping("/respond")
    public ResponseEntity<CaregiverDTO.CaregiverLinkResponse> respondToInvitation(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody CaregiverDTO.RespondRequest request) {

        CaregiverDTO.CaregiverLinkResponse response =
                caregiverService.respondToInvitation(currentUser.getId(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/caregiver/my-patients
     * Returns all active patient delegations for the logged-in caregiver.
     */
    @GetMapping("/my-patients")
    public ResponseEntity<List<CaregiverDTO.DelegatedPatientResponse>> getMyPatients(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        return ResponseEntity.ok(caregiverService.getDelegatedPatients(currentUser.getId()));
    }

    /**
     * POST /api/caregiver/pay-on-behalf
     * Allows a caregiver with PAY_ON_BEHALF permission to pay for a patient's pending order.
     */
    @PostMapping("/pay-on-behalf")
    public ResponseEntity<Map<String, String>> payOnBehalf(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody CaregiverDTO.ProxyPaymentRequest request) {

        String result = caregiverService.payOnBehalf(currentUser.getId(), request);
        return ResponseEntity.ok(Map.of("message", result));
    }

    // ── WhatsApp / SMS Webhook ────────────────────────────────────────────────

    /**
     * POST /api/caregiver/whatsapp-command
     * Inbound webhook from a WhatsApp / SMS gateway (e.g. Twilio, Gupshup, MSG91).
     * Parses the caregiver's text command and executes the action on their behalf.
     *
     * Supported commands: CONFIRM, SKIP, SNOOZE [days], STATUS
     *
     * NOTE: In production, this endpoint should be secured with a gateway-specific
     * shared secret or HMAC signature verification header (e.g. X-Twilio-Signature).
     * For the development environment, it is open to any caller.
     */
    @PostMapping("/whatsapp-command")
    public ResponseEntity<Map<String, String>> processWhatsAppCommand(
            @RequestBody CaregiverDTO.WhatsAppCommandRequest request) {

        String reply = caregiverService.processWhatsAppCommand(request);
        return ResponseEntity.ok(Map.of(
                "reply", reply,
                "from", request.getFrom() != null ? request.getFrom() : "unknown"
        ));
    }

    /**
     * GET /api/caregiver/accept/{accessId}
     * One-click accept endpoint linked from the invitation email.
     * The logged-in user becomes the caregiver for this access record.
     */
    @GetMapping("/accept/{accessId}")
    public ResponseEntity<CaregiverDTO.CaregiverLinkResponse> acceptViaEmailLink(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long accessId) {

        CaregiverDTO.RespondRequest req = new CaregiverDTO.RespondRequest();
        req.setAccessId(accessId);
        req.setAction("ACCEPT");

        CaregiverDTO.CaregiverLinkResponse response =
                caregiverService.respondToInvitation(currentUser.getId(), req);
        return ResponseEntity.ok(response);
    }
}
