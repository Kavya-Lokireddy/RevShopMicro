package com.revshop.product.service.impl;

import com.revshop.product.dto.*;
import com.revshop.product.entity.Category;
import com.revshop.product.entity.Product;
import com.revshop.product.exception.InsufficientStockException;
import com.revshop.product.exception.ResourceNotFoundException;
import com.revshop.product.exception.UnauthorizedException;
import com.revshop.product.repository.CategoryRepository;
import com.revshop.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    private Category category;
    private Product product;

    @BeforeEach
    void setUp() {
        category = new Category(1L, "Electronics", "Gadgets and more");

        product = new Product();
        product.setId(10L);
        product.setName("Watch");
        product.setDescription("Tracks everything");
        product.setPrice(120.0);
        product.setMrp(150.0);
        product.setQuantity(6);
        product.setImageUrl("http://img/watch");
        product.setSellerId(42L);
        product.setCategoryId(category.getId());
        product.setCategory(category);
        product.setActive(true);
        product.setStockThreshold(5);
    }

    @Test
    void addProduct_saves_with_defaults_and_returns_response() {
        ProductRequest request = new ProductRequest(
                "Watch", "Tracks everything", 120.0, 150.0, 6, "http://img/watch", category.getId());

        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product toSave = invocation.getArgument(0);
            toSave.setId(99L);
            return toSave;
        });

        ProductResponse response = productService.addProduct(request, 42L);

        verify(productRepository).save(productCaptor.capture());
        Product saved = productCaptor.getValue();

        assertThat(response.getId()).isEqualTo(99L);
        assertThat(saved.getSellerId()).isEqualTo(42L);
        assertThat(saved.getActive()).isTrue();
        assertThat(saved.getStockThreshold()).isEqualTo(5);
        assertThat(saved.getCategoryId()).isEqualTo(category.getId());
    }

    @Test
    void updateProduct_throws_when_seller_mismatch() {
        Product existing = new Product();
        existing.setId(5L);
        existing.setSellerId(1L);

        ProductUpdateRequest updateRequest = new ProductUpdateRequest(
                "Name", "Desc", 10.0, 12.0, 3, "img", 1L, true);

        when(productRepository.findById(5L)).thenReturn(Optional.of(existing));

        assertThrows(UnauthorizedException.class,
                () -> productService.updateProduct(5L, updateRequest, 2L));

        verify(productRepository, never()).save(any());
    }

    @Test
    void updateStock_throws_when_quantity_goes_negative() {
        product.setQuantity(1);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        assertThrows(InsufficientStockException.class,
                () -> productService.updateStock(product.getId(), -5));

        verify(productRepository, never()).save(any());
    }

    @Test
    void getProductsByCategory_throws_when_category_missing() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.getProductsByCategory(99L, 0, 10));
    }

    @Test
    void getAllProducts_returns_paginated_response() {
        when(productRepository.findByActiveTrue(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1));

        ProductSearchResponse response = productService.getAllProducts(0, 10);

        assertThat(response.getProducts()).hasSize(1);
        assertThat(response.getTotalItems()).isEqualTo(1);
        assertThat(response.getTotalPages()).isEqualTo(1);
        assertThat(response.getCurrentPage()).isEqualTo(0);
    }

    @Test
    void addCategory_throws_when_name_exists() {
        CategoryRequest request = new CategoryRequest("Home", "Home goods");
        when(categoryRepository.existsByName("Home")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> productService.addCategory(request));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void deleteProduct_softDeletes_when_authorized() {
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        productService.deleteProduct(product.getId(), product.getSellerId());

        verify(productRepository).save(productCaptor.capture());
        assertThat(productCaptor.getValue().getActive()).isFalse();
    }

    @Test
    void deleteProduct_throws_when_unauthorized() {
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        assertThrows(UnauthorizedException.class,
                () -> productService.deleteProduct(product.getId(), 999L));

        verify(productRepository, never()).save(any());
    }

    @Test
    void setStockThreshold_updates_when_authorized() {
        ThresholdRequest request = new ThresholdRequest(3);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponse response = productService.setStockThreshold(product.getId(), request, product.getSellerId());

        assertThat(response.getStockThreshold()).isEqualTo(3);
        verify(productRepository).save(product);
    }

    @Test
    void setStockThreshold_throws_when_unauthorized() {
        ThresholdRequest request = new ThresholdRequest(3);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        assertThrows(UnauthorizedException.class,
                () -> productService.setStockThreshold(product.getId(), request, 999L));

        verify(productRepository, never()).save(any());
    }

    @Test
    void getSellerProducts_returns_lowStock_flag() {
        product.setQuantity(2); // below threshold 5
        when(productRepository.findBySellerIdAndActiveTrue(product.getSellerId()))
                .thenReturn(List.of(product));

        List<SellerProductResponse> responses = productService.getSellerProducts(product.getSellerId());

        assertThat(responses).hasSize(1);
        SellerProductResponse resp = responses.get(0);
        assertThat(resp.getLowStock()).isTrue();
        assertThat(resp.getCategoryName()).isEqualTo(category.getName());
    }

    @Test
    void getProductsByIds_filters_inactive_products() {
        Product inactive = new Product();
        inactive.setId(11L);
        inactive.setActive(false);
        inactive.setCategoryId(category.getId());

        when(productRepository.findAllById(List.of(10L, 11L)))
                .thenReturn(List.of(product, inactive));

        List<ProductResponse> responses = productService.getProductsByIds(List.of(10L, 11L));

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(product.getId());
    }

    @Test
    void updateStock_increments_quantity_and_saves() {
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        productService.updateStock(product.getId(), 4);

        verify(productRepository).save(productCaptor.capture());
        assertThat(productCaptor.getValue().getQuantity()).isEqualTo(10);
    }

    @Test
    void updateRating_persists_value() {
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        productService.updateRating(product.getId(), 4.5);

        verify(productRepository).save(productCaptor.capture());
        assertThat(productCaptor.getValue().getRating()).isEqualTo(4.5);
    }

    @Test
    void getProductDetails_throws_when_inactive_or_missing() {
        when(productRepository.findByIdAndActiveTrue(product.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.getProductDetails(product.getId()));
    }
}
