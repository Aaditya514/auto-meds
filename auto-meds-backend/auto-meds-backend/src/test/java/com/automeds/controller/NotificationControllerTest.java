package com.automeds.controller;

import com.automeds.dto.NotificationDTO;
import com.automeds.security.UserPrincipal;
import com.automeds.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    private UserPrincipal mockUser() {
        return new UserPrincipal(1L, "User", "user@test.com", "pass", Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_PATIENT")));
    }

    @Test
    void testGetMyNotificationsAndMarkAsRead() throws Exception {
        Mockito.when(notificationService.getUserNotifications(1L)).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/notifications/my").with(user(mockUser())))
                .andExpect(status().isOk());

        Mockito.when(notificationService.markAsRead(10L, 1L)).thenReturn(new NotificationDTO());
        mockMvc.perform(put("/api/notifications/10/read").with(user(mockUser())))
                .andExpect(status().isOk());
    }
}


