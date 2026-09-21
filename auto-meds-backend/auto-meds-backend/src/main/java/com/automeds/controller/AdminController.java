package com.automeds.controller;

import com.automeds.dto.AdminDashboardDTO;
import com.automeds.dto.MedicineDTO;
import com.automeds.dto.OrderDTO;
import com.automeds.dto.SubscriptionResponseDTO;
import com.automeds.entity.User;
import com.automeds.scheduler.AutoRefillScheduler;
import com.automeds.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: AdminController
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class AdminController {

    private final AdminService adminService;
    private final AutoRefillScheduler autoRefillScheduler;

    public AdminController(AdminService adminService, AutoRefillScheduler autoRefillScheduler) {
        this.adminService = adminService;
        this.autoRefillScheduler = autoRefillScheduler;
    }

    // Handles GET requests at this endpoint
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardDTO> getDashboardMetrics() {
        return ResponseEntity.ok(adminService.getDashboardMetrics());
    }

    // Handles GET requests at this endpoint
    @GetMapping("/subscription-requests")
    public ResponseEntity<List<SubscriptionResponseDTO>> getPendingSubscriptionRequests() {
        return ResponseEntity.ok(adminService.getPendingSubscriptionRequests());
    }

    // Handles PUT requests at this endpoint
    @PutMapping("/subscriptions/{id}/approve")
    public ResponseEntity<List<SubscriptionResponseDTO>> approveSubscription(
            @PathVariable Long id,
            @RequestBody(required = false) List<com.automeds.dto.MedicineAssignmentDTO> assignments) {
        return ResponseEntity.ok(adminService.approveSubscription(id, assignments != null ? assignments : java.util.Collections.emptyList()));
    }

    // Handles PUT requests at this endpoint
    @PutMapping("/subscriptions/{id}/reject")
    public ResponseEntity<SubscriptionResponseDTO> rejectSubscription(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(adminService.rejectSubscription(id, reason));
    }

    // Handles PUT requests at this endpoint
    @PutMapping("/subscriptions/{id}/clarification")
    public ResponseEntity<SubscriptionResponseDTO> requestClarification(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String message = body != null ? body.get("message") : "Please upload a clearer prescription.";
        return ResponseEntity.ok(adminService.requestClarification(id, message));
    }

    // Handles POST requests at this endpoint
    @PostMapping("/medicines")
    public ResponseEntity<MedicineDTO> createMedicine(@Valid @RequestBody MedicineDTO dto) {
        return ResponseEntity.ok(adminService.createMedicine(dto));
    }

    // Handles PUT requests at this endpoint
    @PutMapping("/medicines/{id}")
    public ResponseEntity<MedicineDTO> updateMedicine(@PathVariable Long id, @Valid @RequestBody MedicineDTO dto) {
        return ResponseEntity.ok(adminService.updateMedicine(id, dto));
    }

    // Handles DELETE requests at this endpoint
    @DeleteMapping("/medicines/{id}")
    public ResponseEntity<MedicineDTO> deactivateMedicine(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.deactivateMedicine(id));
    }

    // Handles PUT requests at this endpoint
    @PutMapping("/inventory/{medicineId}/stock")
    public ResponseEntity<MedicineDTO> updateStock(@PathVariable Long medicineId, @RequestBody Map<String, Integer> body) {
        Integer stockQuantity = body.get("stockQuantity");
        return ResponseEntity.ok(adminService.updateStock(medicineId, stockQuantity));
    }

    // Handles GET requests at this endpoint
    @GetMapping("/orders")
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        return ResponseEntity.ok(adminService.getAllOrders());
    }

    // Handles PUT requests at this endpoint
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newStatus = body.get("orderStatus");
        return ResponseEntity.ok(adminService.updateOrderStatus(id, newStatus));
    }

    // Handles GET requests at this endpoint
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllPatients() {
        return ResponseEntity.ok(adminService.getAllPatients());
    }

    // Handles POST requests at this endpoint
    @PostMapping("/scheduler/trigger-refill")
    public ResponseEntity<Map<String, String>> triggerRefillProcess() {
        autoRefillScheduler.processAutoRefills();
        return ResponseEntity.ok(Map.of("message", "Auto-refill process triggered successfully."));
    }
}
