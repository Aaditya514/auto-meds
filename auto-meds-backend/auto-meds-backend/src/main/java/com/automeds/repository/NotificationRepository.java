package com.automeds.repository;

import com.automeds.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: NotificationRepository
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(Long userId, Integer isRead);
}
