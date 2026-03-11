package com.revshop.cart.service.impl;

import com.revshop.cart.client.ProductServiceClient;
import com.revshop.cart.dto.*;
import com.revshop.cart.exception.InsufficientStockException;
import com.revshop.cart.exception.ResourceNotFoundException;
import com.revshop.cart.model.Cart;
import com.revshop.cart.model.CartItem;
import com.revshop.cart.repository.CartItemRepository;
import com.revshop.cart.repository.CartRepository;
import com.revshop.cart.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductServiceClient productServiceClient;

    public CartServiceImpl(CartRepository cartRepository,
                          CartItemRepository cartItemRepository,
                          ProductServiceClient productServiceClient) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productServiceClient = productServiceClient;
    }

    @Override
    @Transactional
    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        log.info("Adding product to cart for userId: {}, productId: {}, quantity: {}", userId, request.getProductId(), request.getQuantity());
        // Get product details from product-service
        ProductDto product = productServiceClient.getProduct(request.getProductId());

        if (product == null || product.getId() == null) {
            throw new ResourceNotFoundException("Product not found with id: " + request.getProductId());
        }

        // Validate stock
        validateStock(product, request.getQuantity());

        // Get or create cart
        Cart cart = getOrCreateCart(userId);

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            // Update existing item
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            validateStock(product, newQuantity);
            item.setQuantity(newQuantity);
        } else {
            // Add new item
            CartItem newItem = new CartItem(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    product.getSellerId(),
                    request.getQuantity()
            );
            cart.addItem(newItem);
        }

        Cart savedCart = cartRepository.save(cart);
        log.info("Product added to cart successfully for userId: {}", userId);
        return mapToResponse(savedCart);
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(Long userId, Long itemId, UpdateCartRequest request) {
        log.info("Updating cart item for userId: {}, itemId: {}, newQuantity: {}", userId, itemId, request.getQuantity());
        Cart cart = getCartForUser(userId);
        CartItem cartItem = findCartItemById(cart, itemId);

        // Get product details to validate stock
        ProductDto product = productServiceClient.getProduct(cartItem.getProductId());
        validateStock(product, request.getQuantity());

        cartItem.setQuantity(request.getQuantity());
        Cart savedCart = cartRepository.save(cart);
        return mapToResponse(savedCart);
    }

    @Override
    @Transactional
    public CartResponse removeFromCart(Long userId, Long itemId) {
        log.info("Removing item from cart for userId: {}, itemId: {}", userId, itemId);
        Cart cart = getCartForUser(userId);
        CartItem cartItem = findCartItemById(cart, itemId);

        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);

        Cart savedCart = cartRepository.save(cart);
        return mapToResponse(savedCart);
    }

    @Override
    public CartResponse getCart(Long userId) {
        log.info("Getting cart for userId: {}", userId);
        return cartRepository.findByUserId(userId)
                .map(this::mapToResponse)
                .orElseGet(this::emptyCartResponse);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        log.info("Clearing cart for userId: {}", userId);
        Cart cart = getCartForUser(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    // Helper Methods
    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart(userId);
                    return cartRepository.save(newCart);
                });
    }

    private Cart getCartForUser(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));
    }

    private CartItem findCartItemById(Cart cart, Long itemId) {
        return cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));
    }

    private void validateStock(ProductDto product, int requestedQuantity) {
        if (product.getQuantity() == null || requestedQuantity > product.getQuantity()) {
            throw new InsufficientStockException(
                    "Requested quantity (" + requestedQuantity +
                            ") exceeds available stock (" + product.getQuantity() + ")"
            );
        }
    }

    private CartResponse emptyCartResponse() {
        return new CartResponse(null, List.of(), 0.0, 0);
    }

    private CartResponse mapToResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::mapItemToResponse)
                .collect(Collectors.toList());

        return new CartResponse(
                cart.getId(),
                items,
                cart.getTotalPrice(),
                cart.getTotalItems()
        );
    }

    private CartItemResponse mapItemToResponse(CartItem item) {
        return new CartItemResponse(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getProductPrice(),
                item.getSellerId(),
                item.getQuantity(),
                item.getSubtotal()
        );
    }
}
