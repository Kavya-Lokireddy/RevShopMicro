package com.revshop.cart.service;

import com.revshop.cart.dto.AddToCartRequest;
import com.revshop.cart.dto.CartResponse;
import com.revshop.cart.dto.UpdateCartRequest;

public interface CartService {

    CartResponse addToCart(Long userId, AddToCartRequest request);

    CartResponse updateCartItem(Long userId, Long itemId, UpdateCartRequest request);

    CartResponse removeFromCart(Long userId, Long itemId);

    CartResponse getCart(Long userId);

    void clearCart(Long userId);
}
