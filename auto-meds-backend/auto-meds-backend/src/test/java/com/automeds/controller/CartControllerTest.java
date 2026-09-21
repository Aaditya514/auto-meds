package com.automeds.controller;

import com.automeds.dto.AddToCartRequest;
import com.automeds.dto.CartDTO;
import com.automeds.dto.UpdateCartItemRequest;
import com.automeds.security.UserPrincipal;
import com.automeds.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    private UserPrincipal mockPrincipal() {
        return new UserPrincipal(1L, "Patient", "patient@test.com", "pass", java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_PATIENT")));
    }

    @Test
    void testGetCart() throws Exception {
        Mockito.when(cartService.getCartByPatientId(1L)).thenReturn(new CartDTO());

        mockMvc.perform(get("/api/cart").with(user(mockPrincipal())))
                .andExpect(status().isOk());
    }

    @Test
    void testAddItemToCart() throws Exception {
        AddToCartRequest req = new AddToCartRequest(2L, 1);
        Mockito.when(cartService.addItemToCart(eq(1L), any())).thenReturn(new CartDTO());

        mockMvc.perform(post("/api/cart/items")
                        .with(user(mockPrincipal()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateCartItemQuantity() throws Exception {
        UpdateCartItemRequest req = new UpdateCartItemRequest();
        req.setQuantity(3);
        Mockito.when(cartService.updateCartItemQuantity(eq(1L), eq(10L), any())).thenReturn(new CartDTO());

        mockMvc.perform(put("/api/cart/items/10")
                        .with(user(mockPrincipal()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void testRemoveCartItem() throws Exception {
        Mockito.when(cartService.removeCartItem(1L, 10L)).thenReturn(new CartDTO());

        mockMvc.perform(delete("/api/cart/items/10").with(user(mockPrincipal())))
                .andExpect(status().isOk());
    }

    @Test
    void testClearCart() throws Exception {
        mockMvc.perform(delete("/api/cart/clear").with(user(mockPrincipal())))
                .andExpect(status().isNoContent());
        Mockito.verify(cartService).clearCart(1L);
    }
}


