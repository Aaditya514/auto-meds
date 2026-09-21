package com.automeds.service;

import com.automeds.dto.NotificationDTO;
import com.automeds.entity.Notification;
import com.automeds.entity.User;
import com.automeds.exception.ResourceNotFoundException;
import com.automeds.repository.NotificationRepository;
import com.automeds.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class NotificationServiceTest {

    private NotificationRepository notificationRepository;
    private UserRepository userRepository;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationRepository = Mockito.mock(NotificationRepository.class);
        userRepository = Mockito.mock(UserRepository.class);

        notificationService = new NotificationService(notificationRepository, userRepository);
    }

    @Test
    void testCreateNotificationSuccess() {
        User u = new User();
        u.setId(1L);

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(u));

        Notification saved = new Notification();
        saved.setId(10L);
        saved.setUser(u);
        saved.setTitle("Title");
        saved.setMessage("Msg");
        saved.setType("INFO");
        saved.setIsRead(0);

        Mockito.when(notificationRepository.save(any())).thenReturn(saved);

        NotificationDTO dto = notificationService.createNotification(1L, "Title", "Msg", null);
        assertNotNull(dto);
        assertEquals(10L, dto.getId());
    }

    @Test
    void testCreateNotificationUserNotFound() {
        Mockito.when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> notificationService.createNotification(99L, "T", "M", "INFO"));
    }

    @Test
    void testGetUserNotificationsAndMarkAsRead() {
        User u = new User();
        u.setId(1L);

        Notification n = new Notification();
        n.setId(10L);
        n.setUser(u);
        n.setIsRead(0);

        Mockito.when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Collections.singletonList(n));
        List<NotificationDTO> list = notificationService.getUserNotifications(1L);
        assertEquals(1, list.size());

        Mockito.when(notificationRepository.findById(10L)).thenReturn(Optional.of(n));
        n.setIsRead(1);
        Mockito.when(notificationRepository.save(n)).thenReturn(n);

        NotificationDTO read = notificationService.markAsRead(10L, 1L);
        assertTrue(read.getIsRead());
    }

    @Test
    void testMarkAsReadUnauthorized() {
        User u1 = new User();
        u1.setId(1L);
        User u2 = new User();
        u2.setId(2L);

        Notification n = new Notification();
        n.setId(10L);
        n.setUser(u1);

        Mockito.when(notificationRepository.findById(10L)).thenReturn(Optional.of(n));
        assertThrows(ResourceNotFoundException.class, () -> notificationService.markAsRead(10L, 2L));
    }
}


