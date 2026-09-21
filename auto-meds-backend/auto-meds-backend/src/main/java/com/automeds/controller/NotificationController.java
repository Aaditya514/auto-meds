package com.automeds.controller;

import com.automeds.dto.NotificationDTO;
import com.automeds.security.UserPrincipal;
import com.automeds.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: NotificationController
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Handles GET requests at this endpoint
    @GetMapping("/my")
    public ResponseEntity<List<NotificationDTO>> getMyNotifications(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userPrincipal.getId()));
    }

    // Handles PUT requests at this endpoint
    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationDTO> markAsRead(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                        @PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id, userPrincipal.getId()));
    }
}
