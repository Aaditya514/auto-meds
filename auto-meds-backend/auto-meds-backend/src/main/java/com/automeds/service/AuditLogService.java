package com.automeds.service;

import com.automeds.entity.AuditLog;
import com.automeds.repository.AuditLogRepository;
import com.automeds.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Record an immutable audit log entry using explicit actor parameters.
     */
    @Transactional
    public AuditLog recordLog(Long actorId, String actorName, String actorRole, String action, 
                              String resourceType, Long resourceId, String details, String ipAddress) {
        AuditLog auditLog = new AuditLog(actorId, actorName, actorRole, action, resourceType, resourceId, details, ipAddress);
        AuditLog saved = auditLogRepository.save(auditLog);
        log.info("AUDIT LOG [{}] - Actor: {} ({}) | Resource: {} #{} | Details: {}", 
                action, actorName, actorRole, resourceType, resourceId, details);
        return saved;
    }

    /**
     * Convenience method to record an audit log using the current SecurityContext authentication.
     */
    @Transactional
    public AuditLog recordCurrentAction(String action, String resourceType, Long resourceId, String details) {
        Long actorId = null;
        String actorName = "SYSTEM";
        String actorRole = "SYSTEM";

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            actorId = principal.getId();
            actorName = principal.getName() != null ? principal.getName() : principal.getEmail();
            actorRole = principal.getAuthorities().stream()
                    .findFirst()
                    .map(a -> a.getAuthority().replace("ROLE_", ""))
                    .orElse("USER");
        }

        return recordLog(actorId, actorName, actorRole, action, resourceType, resourceId, details, "127.0.0.1");
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getRecentLogs() {
        return auditLogRepository.findTop100ByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<AuditLog> searchLogs(String query) {
        if (query == null || query.isBlank()) {
            return getRecentLogs();
        }
        return auditLogRepository.searchAuditLogs(query.trim());
    }

    /**
     * Convenience overload for service-layer audit logging when the full actor name
     * is not readily available (e.g. background processing or proxy operations).
     * Actor name defaults to actorId.toString().
     */
    @Transactional
    public AuditLog log(Long actorId, String actorRole, String action,
                        String resourceType, Long resourceId, String details) {
        String actorName = actorId != null ? actorId.toString() : "SYSTEM";
        return recordLog(actorId, actorName, actorRole, action,
                         resourceType, resourceId, details, "internal");
    }
}
