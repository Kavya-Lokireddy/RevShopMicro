package com.revshop.product.service;

import com.revshop.product.dto.*;
import com.revshop.product.entity.Category;

import java.util.List;

public interface ProductService {

    // ========== Seller Operations (Kavya) ==========

    ProductResponse addProduct(ProductRequest request, Long sellerId);

    ProductResponse updateProduct(Long id, ProductUpdateRequest request, Long sellerId);

    void deleteProduct(Long id, Long sellerId);

    ProductResponse setStockThreshold(Long id, ThresholdRequest request, Long sellerId);

    List<SellerProductResponse> getSellerProducts(Long sellerId);

    // ========== Buyer Operations (Jatin) ==========

    ProductSearchResponse getAllProducts(int page, int size);

    ProductResponse getProductDetails(Long id);

    ProductSearchResponse getProductsByCategory(Long categoryId, int page, int size);

    List<ProductResponse> searchProducts(String keyword);

    List<ProductResponse> getProductsByIds(List<Long> ids);

    // ========== Internal Operations ==========

    ProductResponse getProductById(Long id);

    void updateStock(Long id, Integer quantity);

    void updateRating(Long id, Double rating);

    // ========== Category Operations ==========

    List<CategoryResponse> getAllCategories();

    CategoryResponse addCategory(CategoryRequest request);

    Category getCategoryById(Long id);
}
