package com.automeds.controller;

import com.automeds.dto.SubscriptionResponseDTO;
import com.automeds.security.UserPrincipal;
import com.automeds.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubscriptionService subscriptionService;

    private UserPrincipal mockPatient() {
        return new UserPrincipal(1L, "Patient", "patient@test.com", "pass", Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_PATIENT")));
    }

    @Test
    void testCreateSubscriptionEndpoint() throws Exception {
        MockMultipartFile file = new MockMultipartFile("prescriptionFile", "p.pdf", "application/pdf", "data".getBytes());
        SubscriptionResponseDTO res = new SubscriptionResponseDTO();
        res.setId(10L);

        Mockito.when(subscriptionService.createSubscription(eq(1L), any(), any())).thenReturn(res);

        mockMvc.perform(multipart("/api/subscriptions")
                        .file(file)
                        .param("medicineId", "2")
                        .param("dosage", "1 tablet")
                        .param("frequency", "Daily")
                        .param("quantity", "30")
                        .with(user(mockPatient())))
                .andExpect(status().isOk());
    }

    @Test
    void testGetMySubscriptions() throws Exception {
        Mockito.when(subscriptionService.getSubscriptionsForPatient(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/subscriptions/my").with(user(mockPatient())))
                .andExpect(status().isOk());
    }

    @Test
    void testGetSubscriptionById() throws Exception {
        SubscriptionResponseDTO dto = new SubscriptionResponseDTO();
        dto.setId(10L);
        Mockito.when(subscriptionService.getSubscriptionById(10L)).thenReturn(dto);

        mockMvc.perform(get("/api/subscriptions/10").with(user(mockPatient())))
                .andExpect(status().isOk());
    }

    @Test
    void testLifecycleActions() throws Exception {
        Mockito.when(subscriptionService.cancelSubscription(1L, 10L)).thenReturn(new SubscriptionResponseDTO());
        Mockito.when(subscriptionService.pauseSubscription(1L, 10L)).thenReturn(new SubscriptionResponseDTO());
        Mockito.when(subscriptionService.resumeSubscription(1L, 10L)).thenReturn(new SubscriptionResponseDTO());
        Mockito.when(subscriptionService.renewSubscription(eq(1L), eq(10L), any())).thenReturn(new SubscriptionResponseDTO());

        mockMvc.perform(put("/api/subscriptions/10/cancel").with(user(mockPatient())))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/subscriptions/10/pause").with(user(mockPatient())))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/subscriptions/10/resume").with(user(mockPatient())))
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/subscriptions/10/renew")
                        .with(user(mockPatient()))
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk());
    }
}


