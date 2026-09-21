package com.automeds.repository;

import com.automeds.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: SubscriptionRepository
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    List<Subscription> findByStatus(String status);
    List<Subscription> findByStatusAndNextRefillDateLessThanEqual(String status, LocalDateTime dateThreshold);
    Long countByStatus(String status);
}
