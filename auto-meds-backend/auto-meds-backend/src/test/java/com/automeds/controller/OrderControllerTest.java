package com.automeds.controller;

import com.automeds.dto.CheckoutRequest;
import com.automeds.dto.OrderDTO;
import com.automeds.security.UserPrincipal;
import com.automeds.service.OrderService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    private UserPrincipal mockPatient() {
        return new UserPrincipal(1L, "Patient", "patient@test.com", "pass", Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_PATIENT")));
    }

    @Test
    void testCheckout() throws Exception {
        CheckoutRequest req = new CheckoutRequest("123 St", "COD");
        OrderDTO dto = new OrderDTO();
        dto.setId(100L);

        Mockito.when(orderService.checkoutCart(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(post("/api/orders/checkout")
                        .with(user(mockPatient()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetMyOrdersAndOrderById() throws Exception {
        Mockito.when(orderService.getOrdersForPatient(1L)).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/orders/my").with(user(mockPatient())))
                .andExpect(status().isOk());

        OrderDTO dto = new OrderDTO();
        dto.setId(100L);
        Mockito.when(orderService.getOrderById(100L)).thenReturn(dto);
        mockMvc.perform(get("/api/orders/100").with(user(mockPatient())))
                .andExpect(status().isOk());
    }
}

