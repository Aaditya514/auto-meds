package com.automeds.controller;

import com.automeds.security.UserPrincipal;
import com.automeds.service.SseNotificationService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notifications")
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: SseController
 * Description: Controller exposing Server-Sent Events (SSE) streaming endpoints (P4)
 * for reactive real-time browser updates without continuous polling.
 */
public class SseController {

    private final SseNotificationService sseNotificationService;

    public SseController(SseNotificationService sseNotificationService) {
        this.sseNotificationService = sseNotificationService;
    }

    /**
     * Patient SSE channel: receives prescription approvals, OCR processing results,
     * refill window notices, and live order status changes.
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamPatientEvents(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            throw new IllegalArgumentException("User must be authenticated to subscribe to real-time events");
        }
        return sseNotificationService.subscribePatient(userPrincipal.getId());
    }

    /**
     * Clinical Staff SSE channel: receives urgent stockout alerts, newly submitted prescriptions
     * awaiting triage, and automated refill execution logs.
     */
    @GetMapping(value = "/staff-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public SseEmitter streamStaffEvents(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return sseNotificationService.subscribeStaff();
    }
}
