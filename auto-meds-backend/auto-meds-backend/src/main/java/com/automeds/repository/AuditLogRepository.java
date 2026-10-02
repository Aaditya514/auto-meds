package com.automeds.repository;

import com.automeds.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findTop100ByOrderByCreatedAtDesc();

    List<AuditLog> findByActionOrderByCreatedAtDesc(String action);

    List<AuditLog> findByResourceTypeAndResourceIdOrderByCreatedAtDesc(String resourceType, Long resourceId);

    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:query IS NULL OR LOWER(a.action) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(a.actorName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(a.details) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(a.resourceType) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY a.createdAt DESC")
    List<AuditLog> searchAuditLogs(@Param("query") String query);
}
