package com.revshop.cart.service.impl;

import com.revshop.cart.client.ProductServiceClient;
import com.revshop.cart.dto.FavoriteResponse;
import com.revshop.cart.dto.ProductDto;
import com.revshop.cart.exception.DuplicateResourceException;
import com.revshop.cart.exception.ResourceNotFoundException;
import com.revshop.cart.model.Favorite;
import com.revshop.cart.repository.FavoriteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceImplTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @InjectMocks
    private FavoriteServiceImpl favoriteService;

    private Favorite testFavorite;
    private ProductDto testProduct;

    @BeforeEach
    void setUp() {
        testFavorite = new Favorite(1L, 1L, "Test Product", 29.99, "http://example.com/img.jpg");
        testFavorite.setId(1L);
        testFavorite.setAddedAt(LocalDateTime.now());

        testProduct = new ProductDto();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(29.99);
        testProduct.setQuantity(100);
        testProduct.setSellerId(10L);
        testProduct.setImageUrl("http://example.com/img.jpg");
    }

    @Test
    @DisplayName("Test addFavorite - should add favorite successfully")
    void testAddFavorite_Success() {
        when(favoriteRepository.existsByUserIdAndProductId(1L, 1L)).thenReturn(false);
        when(productServiceClient.getProduct(1L)).thenReturn(testProduct);
        when(favoriteRepository.save(any(Favorite.class))).thenReturn(testFavorite);

        FavoriteResponse response = favoriteService.addFavorite(1L, 1L);

        assertNotNull(response);
        assertEquals(1L, response.getProductId());
        assertEquals("Test Product", response.getProductName());
        assertEquals(29.99, response.getProductPrice());
        verify(favoriteRepository).save(any(Favorite.class));
    }

    @Test
    @DisplayName("Test addFavorite - should throw exception for duplicate favorite")
    void testAddFavorite_Duplicate() {
        when(favoriteRepository.existsByUserIdAndProductId(1L, 1L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> favoriteService.addFavorite(1L, 1L));
        verify(favoriteRepository, never()).save(any(Favorite.class));
    }

    @Test
    @DisplayName("Test addFavorite - should throw exception when product not found")
    void testAddFavorite_ProductNotFound() {
        ProductDto nullProduct = new ProductDto();
        nullProduct.setId(null);

        when(favoriteRepository.existsByUserIdAndProductId(1L, 1L)).thenReturn(false);
        when(productServiceClient.getProduct(1L)).thenReturn(nullProduct);

        assertThrows(ResourceNotFoundException.class, () -> favoriteService.addFavorite(1L, 1L));
    }

    @Test
    @DisplayName("Test removeFavorite - should remove favorite successfully")
    void testRemoveFavorite_Success() {
        when(favoriteRepository.findByUserIdAndProductId(1L, 1L)).thenReturn(Optional.of(testFavorite));

        favoriteService.removeFavorite(1L, 1L);

        verify(favoriteRepository).delete(testFavorite);
    }

    @Test
    @DisplayName("Test removeFavorite - should throw exception when not found")
    void testRemoveFavorite_NotFound() {
        when(favoriteRepository.findByUserIdAndProductId(1L, 999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> favoriteService.removeFavorite(1L, 999L));
    }

    @Test
    @DisplayName("Test getFavorites - should return all favorites for user")
    void testGetFavorites_Success() {
        when(favoriteRepository.findByUserId(1L)).thenReturn(List.of(testFavorite));

        List<FavoriteResponse> responses = favoriteService.getFavorites(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Test Product", responses.get(0).getProductName());
    }

    @Test
    @DisplayName("Test isFavorite - should return true when product is favorited")
    void testIsFavorite_True() {
        when(favoriteRepository.existsByUserIdAndProductId(1L, 1L)).thenReturn(true);

        assertTrue(favoriteService.isFavorite(1L, 1L));
    }

    @Test
    @DisplayName("Test isFavorite - should return false when product is not favorited")
    void testIsFavorite_False() {
        when(favoriteRepository.existsByUserIdAndProductId(1L, 999L)).thenReturn(false);

        assertFalse(favoriteService.isFavorite(1L, 999L));
    }
}
