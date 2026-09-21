package com.automeds.repository;

import com.automeds.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: OrderRepository
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByPatientIdOrderByOrderDateDesc(Long patientId);
    List<Order> findAllByOrderByOrderDateDesc();
    List<Order> findByOrderStatus(String orderStatus);
    List<Order> findByOrderType(String orderType);
    Long countByOrderStatus(String orderStatus);
}
