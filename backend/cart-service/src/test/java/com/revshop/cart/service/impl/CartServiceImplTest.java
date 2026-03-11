package com.revshop.cart.service.impl;

import com.revshop.cart.client.ProductServiceClient;
import com.revshop.cart.dto.*;
import com.revshop.cart.exception.InsufficientStockException;
import com.revshop.cart.exception.ResourceNotFoundException;
import com.revshop.cart.model.Cart;
import com.revshop.cart.model.CartItem;
import com.revshop.cart.repository.CartItemRepository;
import com.revshop.cart.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart testCart;
    private CartItem testCartItem;
    private ProductDto testProduct;
    private AddToCartRequest addToCartRequest;

    @BeforeEach
    void setUp() {
        // Setup test product
        testProduct = new ProductDto();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(29.99);
        testProduct.setQuantity(100);
        testProduct.setSellerId(10L);
        testProduct.setImageUrl("http://example.com/img.jpg");

        // Setup test cart item
        testCartItem = new CartItem(1L, "Test Product", 29.99, 10L, 2);
        testCartItem.setId(1L);
        testCartItem.setCreatedAt(LocalDateTime.now());

        // Setup test cart
        testCart = new Cart(1L);
        testCart.setId(1L);
        testCart.setCreatedAt(LocalDateTime.now());
        testCart.setUpdatedAt(LocalDateTime.now());
        testCart.setItems(new ArrayList<>());
        testCart.addItem(testCartItem);

        // Setup add to cart request
        addToCartRequest = new AddToCartRequest();
        addToCartRequest.setProductId(1L);
        addToCartRequest.setQuantity(2);
    }

    @Test
    @DisplayName("Test addToCart - should add new item to cart")
    void testAddToCart_NewItem() {
        AddToCartRequest newItemRequest = new AddToCartRequest();
        newItemRequest.setProductId(2L);
        newItemRequest.setQuantity(1);

        ProductDto newProduct = new ProductDto();
        newProduct.setId(2L);
        newProduct.setName("New Product");
        newProduct.setPrice(49.99);
        newProduct.setQuantity(50);
        newProduct.setSellerId(20L);

        when(productServiceClient.getProduct(2L)).thenReturn(newProduct);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        CartResponse response = cartService.addToCart(1L, newItemRequest);

        assertNotNull(response);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("Test addToCart - should update quantity for existing item")
    void testAddToCart_ExistingItem() {
        when(productServiceClient.getProduct(1L)).thenReturn(testProduct);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        CartResponse response = cartService.addToCart(1L, addToCartRequest);

        assertNotNull(response);
        assertEquals(4, testCartItem.getQuantity()); // 2 (existing) + 2 (new)
    }

    @Test
    @DisplayName("Test addToCart - should throw exception for insufficient stock")
    void testAddToCart_InsufficientStock() {
        testProduct.setQuantity(1); // Only 1 in stock
        addToCartRequest.setQuantity(5);

        when(productServiceClient.getProduct(1L)).thenReturn(testProduct);

        assertThrows(InsufficientStockException.class, () -> cartService.addToCart(1L, addToCartRequest));
    }

    @Test
    @DisplayName("Test addToCart - should throw exception when product not found")
    void testAddToCart_ProductNotFound() {
        ProductDto nullProduct = new ProductDto();
        nullProduct.setId(null);

        when(productServiceClient.getProduct(1L)).thenReturn(nullProduct);

        assertThrows(ResourceNotFoundException.class, () -> cartService.addToCart(1L, addToCartRequest));
    }

    @Test
    @DisplayName("Test getCart - should return cart for user")
    void testGetCart_Success() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));

        CartResponse response = cartService.getCart(1L);

        assertNotNull(response);
        assertEquals(1L, response.getCartId());
        assertFalse(response.getItems().isEmpty());
    }

    @Test
    @DisplayName("Test getCart - should return empty cart when no cart exists")
    void testGetCart_EmptyCart() {
        when(cartRepository.findByUserId(999L)).thenReturn(Optional.empty());

        CartResponse response = cartService.getCart(999L);

        assertNotNull(response);
        assertTrue(response.getItems().isEmpty());
        assertEquals(0.0, response.getTotalPrice());
    }

    @Test
    @DisplayName("Test removeFromCart - should remove item from cart")
    void testRemoveFromCart_Success() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        CartResponse response = cartService.removeFromCart(1L, 1L);

        assertNotNull(response);
        verify(cartItemRepository).delete(testCartItem);
    }

    @Test
    @DisplayName("Test clearCart - should clear all items from cart")
    void testClearCart_Success() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));

        cartService.clearCart(1L);

        assertTrue(testCart.getItems().isEmpty());
        verify(cartRepository).save(testCart);
    }
}
