package com.automeds.controller;

import com.automeds.entity.AuditLog;
import com.automeds.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/admin/audit-logs", "/api/audit-logs"})
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<List<AuditLog>> getAuditLogs(@RequestParam(value = "query", required = false) String query) {
        return ResponseEntity.ok(auditLogService.searchLogs(query));
    }
}
