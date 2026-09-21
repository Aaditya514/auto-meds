package com.automeds.controller;

import com.automeds.dto.AdminDashboardDTO;
import com.automeds.dto.MedicineDTO;
import com.automeds.dto.OrderDTO;
import com.automeds.dto.SubscriptionResponseDTO;
import com.automeds.scheduler.AutoRefillScheduler;
import com.automeds.security.UserPrincipal;
import com.automeds.service.AdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminService adminService;

    @MockBean
    private AutoRefillScheduler autoRefillScheduler;

    private UserPrincipal mockAdmin() {
        return new UserPrincipal(99L, "Admin", "admin@test.com", "pass", Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void testAdminEndpoints() throws Exception {
        Mockito.when(adminService.getDashboardMetrics()).thenReturn(new AdminDashboardDTO(1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L));
        mockMvc.perform(get("/api/admin/dashboard").with(user(mockAdmin())))
                .andExpect(status().isOk());

        Mockito.when(adminService.getPendingSubscriptionRequests()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/admin/subscription-requests").with(user(mockAdmin())))
                .andExpect(status().isOk());

        Mockito.when(adminService.approveSubscription(eq(10L), any())).thenReturn(Collections.emptyList());
        mockMvc.perform(put("/api/admin/subscriptions/10/approve").with(user(mockAdmin())))
                .andExpect(status().isOk());

        Mockito.when(adminService.rejectSubscription(eq(10L), any())).thenReturn(new SubscriptionResponseDTO());
        mockMvc.perform(put("/api/admin/subscriptions/10/reject")
                        .with(user(mockAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("reason", "bad image"))))
                .andExpect(status().isOk());

        Mockito.when(adminService.requestClarification(eq(10L), any())).thenReturn(new SubscriptionResponseDTO());
        mockMvc.perform(put("/api/admin/subscriptions/10/clarification")
                        .with(user(mockAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("message", "re-upload"))))
                .andExpect(status().isOk());

        MedicineDTO mDto = new MedicineDTO();
        mDto.setMedicineName("Med");
        mDto.setBrandName("Brand");
        mDto.setComposition("Comp");
        mDto.setStrength("10mg");
        mDto.setPrice(java.math.BigDecimal.TEN);
        Mockito.when(adminService.createMedicine(any())).thenReturn(mDto);
        mockMvc.perform(post("/api/admin/medicines")
                        .with(user(mockAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mDto)))
                .andExpect(status().isOk());

        Mockito.when(adminService.updateMedicine(eq(1L), any())).thenReturn(mDto);
        mockMvc.perform(put("/api/admin/medicines/1")
                        .with(user(mockAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mDto)))
                .andExpect(status().isOk());

        Mockito.when(adminService.deactivateMedicine(1L)).thenReturn(mDto);
        mockMvc.perform(delete("/api/admin/medicines/1").with(user(mockAdmin())))
                .andExpect(status().isOk());

        Mockito.when(adminService.updateStock(1L, 20)).thenReturn(mDto);
        mockMvc.perform(put("/api/admin/inventory/1/stock")
                        .with(user(mockAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("stockQuantity", 20))))
                .andExpect(status().isOk());

        Mockito.when(adminService.getAllOrders()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/admin/orders").with(user(mockAdmin())))
                .andExpect(status().isOk());

        Mockito.when(adminService.updateOrderStatus(eq(100L), any())).thenReturn(new OrderDTO());
        mockMvc.perform(put("/api/admin/orders/100/status")
                        .with(user(mockAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("orderStatus", "DELIVERED"))))
                .andExpect(status().isOk());

        Mockito.when(adminService.getAllPatients()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/admin/users").with(user(mockAdmin())))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/admin/scheduler/trigger-refill").with(user(mockAdmin())))
                .andExpect(status().isOk());
        Mockito.verify(autoRefillScheduler).processAutoRefills();
    }
}


