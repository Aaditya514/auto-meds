package com.automeds.controller;

import com.automeds.dto.AddToCartRequest;
import com.automeds.dto.CartDTO;
import com.automeds.dto.UpdateCartItemRequest;
import com.automeds.security.UserPrincipal;
import com.automeds.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: CartController
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartDTO> getCart(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(cartService.getCartByPatientId(userPrincipal.getId()));
    }

    // Handles POST requests at this endpoint
    @PostMapping("/items")
    public ResponseEntity<CartDTO> addItemToCart(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                 @Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(cartService.addItemToCart(userPrincipal.getId(), request));
    }

    // Handles PUT requests at this endpoint
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartDTO> updateItemQuantity(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                       @PathVariable Long itemId,
                                                       @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateCartItemQuantity(userPrincipal.getId(), itemId, request));
    }

    // Handles DELETE requests at this endpoint
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartDTO> removeItem(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                               @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeCartItem(userPrincipal.getId(), itemId));
    }

    // Handles DELETE requests at this endpoint
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        cartService.clearCart(userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }
}
