package com.automeds.controller;

import com.automeds.dto.CheckoutRequest;
import com.automeds.dto.OrderDTO;
import com.automeds.security.UserPrincipal;
import com.automeds.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: OrderController
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Handles POST requests at this endpoint
    @PostMapping("/checkout")
    public ResponseEntity<OrderDTO> checkoutCart(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(orderService.checkoutCart(userPrincipal.getId(), request));
    }

    // Handles GET requests at this endpoint
    @GetMapping("/my")
    public ResponseEntity<List<OrderDTO>> getMyOrders(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(orderService.getOrdersForPatient(userPrincipal.getId()));
    }

    // Handles GET requests at this endpoint
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }
}
