package com.automeds.repository;

import com.automeds.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: CartRepository
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByPatientId(Long patientId);
}
