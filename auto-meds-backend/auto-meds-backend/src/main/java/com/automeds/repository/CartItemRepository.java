package com.automeds.repository;

import com.automeds.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: CartItemRepository
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartIdAndMedicineId(Long cartId, Long medicineId);
    List<CartItem> findByCartId(Long cartId);
    void deleteByCartId(Long cartId);
}
