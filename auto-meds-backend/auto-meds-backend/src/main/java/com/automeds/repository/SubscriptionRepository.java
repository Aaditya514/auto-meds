package com.automeds.repository;

import com.automeds.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: SubscriptionRepository
 * Description: Repository with custom queries for tracking active subscriptions and pending soft-lock reservations.
 */
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    List<Subscription> findByStatus(String status);
    List<Subscription> findByStatusAndNextRefillDateLessThanEqual(String status, LocalDateTime dateThreshold);
    List<Subscription> findByStatusAndReservationStatus(String status, String reservationStatus);

    @Query("SELECT s FROM Subscription s WHERE s.status = :status AND (s.reservationStatus IS NULL OR s.reservationStatus = 'NONE') AND s.nextRefillDate <= :dateThreshold")
    List<Subscription> findPendingReservations(@Param("status") String status, @Param("dateThreshold") LocalDateTime dateThreshold);

    List<Subscription> findByMedicineIdAndStatusAndReservationStatusOrderByNextRefillDateAsc(Long medicineId, String status, String reservationStatus);
    Long countByMedicineIdAndStatusAndReservationStatus(Long medicineId, String status, String reservationStatus);

    Long countByStatus(String status);
}
