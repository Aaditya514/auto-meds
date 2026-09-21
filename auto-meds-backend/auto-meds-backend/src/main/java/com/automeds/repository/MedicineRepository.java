package com.automeds.repository;

import com.automeds.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: MedicineRepository
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    List<Medicine> findByActive(Integer active);

    @Query("SELECT m FROM Medicine m WHERE m.active = 1 AND (" +
           "LOWER(m.medicineName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(m.brandName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(m.composition) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(m.strength) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Medicine> searchMedicines(@Param("query") String query);

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
}
