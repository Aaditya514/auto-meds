package com.automeds.service;

import com.automeds.dto.NotificationDTO;
import com.automeds.entity.Notification;
import com.automeds.entity.User;
import com.automeds.exception.ResourceNotFoundException;
import com.automeds.repository.NotificationRepository;
import com.automeds.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: NotificationService
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    // Wraps execution inside a database transaction
    @Transactional
    public NotificationDTO createNotification(Long userId, String title, String message, String type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type != null ? type : "INFO");
        notification.setIsRead(0);

        Notification saved = notificationRepository.save(notification);
        return convertToDTO(saved);
    }

    public List<NotificationDTO> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToDTO)
                .toList();
    }

    // Wraps execution inside a database transaction
    @Transactional
    public NotificationDTO markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        if (!notification.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Notification", "id", notificationId);
        }

        notification.setIsRead(1);
        return convertToDTO(notificationRepository.save(notification));
    }

    public NotificationDTO convertToDTO(Notification notification) {
        return new NotificationDTO(
                notification.getId(),
                notification.getUser().getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.getIsRead() != null && notification.getIsRead() == 1,
                notification.getCreatedAt()
        );
    }
}
