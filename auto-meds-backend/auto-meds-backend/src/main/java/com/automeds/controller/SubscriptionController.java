package com.automeds.controller;

import com.automeds.dto.SubscriptionRequestDTO;
import com.automeds.dto.SubscriptionResponseDTO;
import com.automeds.security.UserPrincipal;
import com.automeds.service.SubscriptionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: SubscriptionController
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    // Handles POST requests at this endpoint
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SubscriptionResponseDTO> createSubscription(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(value = "medicineId", required = false) Long medicineId,
            @RequestParam(value = "dosage", required = false) String dosage,
            @RequestParam(value = "frequency", required = false) String frequency,
            @RequestParam(value = "quantity", required = false) Integer quantity,
            @RequestParam(value = "doctorVisitDate", required = false) String doctorVisitDateStr,
            @RequestPart("prescriptionFile") MultipartFile prescriptionFile) {

        java.time.LocalDateTime doctorVisitDate = null;
        if (doctorVisitDateStr != null && !doctorVisitDateStr.trim().isEmpty()) {
            try {
                if (doctorVisitDateStr.contains("T")) {
                    doctorVisitDate = java.time.LocalDateTime.parse(doctorVisitDateStr);
                } else {
                    doctorVisitDate = java.time.LocalDate.parse(doctorVisitDateStr).atStartOfDay();
                }
            } catch (Exception e) {
                // Keep as null if unparseable
            }
        }

        SubscriptionRequestDTO request = new SubscriptionRequestDTO(medicineId, dosage, frequency, quantity, doctorVisitDate);
        SubscriptionResponseDTO response = subscriptionService.createSubscription(userPrincipal.getId(), request, prescriptionFile);
        return ResponseEntity.ok(response);
    }

    // Handles GET requests at this endpoint
    @GetMapping("/my")
    public ResponseEntity<List<SubscriptionResponseDTO>> getMySubscriptions(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionsForPatient(userPrincipal.getId()));
    }

    // Handles GET requests at this endpoint
    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionResponseDTO> getSubscriptionById(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionById(id));
    }

    // Handles PUT requests at this endpoint
    @PutMapping("/{id}/cancel")
    public ResponseEntity<SubscriptionResponseDTO> cancelSubscription(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.cancelSubscription(userPrincipal.getId(), id));
    }

    // Handles PUT requests at this endpoint
    @PutMapping("/{id}/pause")
    public ResponseEntity<SubscriptionResponseDTO> pauseSubscription(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.pauseSubscription(userPrincipal.getId(), id));
    }

    // Handles PUT requests at this endpoint
    @PutMapping("/{id}/resume")
    public ResponseEntity<SubscriptionResponseDTO> resumeSubscription(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.resumeSubscription(userPrincipal.getId(), id));
    }

    // Handles PUT requests at this endpoint
    @PutMapping(value = "/{id}/renew", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SubscriptionResponseDTO> renewSubscription(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @RequestPart(value = "prescriptionFile", required = false) MultipartFile prescriptionFile) {
        return ResponseEntity.ok(subscriptionService.renewSubscription(userPrincipal.getId(), id, prescriptionFile));
    }
}
