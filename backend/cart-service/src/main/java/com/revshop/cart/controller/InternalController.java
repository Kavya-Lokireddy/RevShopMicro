package com.revshop.cart.controller;

import com.revshop.cart.dto.CartResponse;
import com.revshop.cart.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal/cart")
public class InternalController {

    private final CartService cartService;

    public InternalController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * Internal endpoint for checkout-service to retrieve user's cart
     */
    @GetMapping("/{userId}")
    public ResponseEntity<CartResponse> getCartByUserId(@PathVariable Long userId) {
        CartResponse response = cartService.getCart(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Internal endpoint for checkout-service to clear cart after order completion
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> clearCartByUserId(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
