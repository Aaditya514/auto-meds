package com.automeds.repository;

import com.automeds.entity.Medicine;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: MedicineRepository
 * Description: Repository with atomic decrement queries and pessimistic locking for high-concurrency stock management.
 */
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    List<Medicine> findByActive(Integer active);

    @Query("SELECT m FROM Medicine m WHERE m.active = 1 AND (" +
           "LOWER(m.medicineName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(m.brandName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(m.composition) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(m.strength) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "(m.symptoms IS NOT NULL AND LOWER(m.symptoms) LIKE LOWER(CONCAT('%', :query, '%'))))")
    List<Medicine> searchMedicines(@Param("query") String query);

    @Query("SELECT m FROM Medicine m WHERE m.active = 1 AND m.symptoms IS NOT NULL AND LOWER(m.symptoms) LIKE LOWER(CONCAT('%', :symptom, '%'))")
    List<Medicine> findBySymptom(@Param("symptom") String symptom);

    /**
     * Finds alternative medicine brands that match EXACT SAME composition AND EXACT SAME strength,
     * excluding the specified medicineId.
     */
    @Query("SELECT m FROM Medicine m WHERE m.active = 1 AND m.id <> :medicineId AND " +
           "LOWER(TRIM(m.composition)) = LOWER(TRIM(:composition)) AND " +
           "LOWER(TRIM(m.strength)) = LOWER(TRIM(:strength)) " +
           "ORDER BY m.stockQuantity DESC, m.price ASC")
    List<Medicine> findAlternatives(@Param("composition") String composition, 
                                     @Param("strength") String strength, 
                                     @Param("medicineId") Long medicineId);

    List<Medicine> findByActiveAndStockQuantityLessThanEqual(Integer active, Integer threshold);

    List<Medicine> findByActiveAndStockQuantityEquals(Integer active, Integer stockQuantity);

    /**
     * Pessimistic Write Lock: Acquires row-level database lock during critical checkout paths.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Medicine m WHERE m.id = :id")
    Optional<Medicine> findByIdForUpdate(@Param("id") Long id);

    /**
     * Atomic Available Stock Decrement:
     * Ensures stock is decremented ONLY if available stock (stock - reserved) >= requested quantity.
     * Prevents TOCTOU race conditions and protects stock reserved for chronic subscribers.
     * Returns 1 if updated, 0 if insufficient available stock.
     */
    @Modifying
    @Query("UPDATE Medicine m SET m.stockQuantity = m.stockQuantity - :qty " +
           "WHERE m.id = :id AND (m.stockQuantity - m.reservedQuantity) >= :qty")
    int deductAvailableStock(@Param("id") Long id, @Param("qty") Integer qty);

    /**
     * Atomic Reserved Stock Decrement (Refill Execution on Day 0):
     * Deducts from BOTH physical stock and reserved stock when fulfilling a pre-allocated subscription.
     */
    @Modifying
    @Query("UPDATE Medicine m SET m.stockQuantity = m.stockQuantity - :qty, " +
           "m.reservedQuantity = m.reservedQuantity - :qty " +
           "WHERE m.id = :id AND m.stockQuantity >= :qty AND m.reservedQuantity >= :qty")
    int deductReservedStock(@Param("id") Long id, @Param("qty") Integer qty);

    /**
     * 5-Day Soft-Lock Reservation:
     * Atomically locks stock for upcoming subscription refill without deducting physical inventory.
     */
    @Modifying
    @Query("UPDATE Medicine m SET m.reservedQuantity = m.reservedQuantity + :qty " +
           "WHERE m.id = :id AND (m.stockQuantity - m.reservedQuantity) >= :qty")
    int reserveStock(@Param("id") Long id, @Param("qty") Integer qty);

    /**
     * Release Reserved Stock:
     * Restores soft-locked quantity back to available inventory if patient cancels, pauses, or snoozes.
     */
    @Modifying
    @Query("UPDATE Medicine m SET m.reservedQuantity = CASE WHEN m.reservedQuantity >= :qty THEN m.reservedQuantity - :qty ELSE 0 END " +
           "WHERE m.id = :id")
    int releaseReservedStock(@Param("id") Long id, @Param("qty") Integer qty);

    /**
     * Query medicines where available stock is at or below the reorder threshold.
     */
    @Query("SELECT m FROM Medicine m WHERE m.active = 1 AND (m.stockQuantity - m.reservedQuantity) <= m.reorderThreshold")
    List<Medicine> findMedicinesNeedingReorder();

    /**
     * Near-Expiry Alert: medicines expiring within the next N days.
     * Used by the admin dashboard to flag stock that should be prioritised for dispatch or written off.
     */
    @Query("SELECT m FROM Medicine m WHERE m.active = 1 AND m.expiryDate IS NOT NULL AND m.expiryDate <= :threshold ORDER BY m.expiryDate ASC")
    List<Medicine> findNearExpiryMedicines(@Param("threshold") java.time.LocalDateTime threshold);
}
