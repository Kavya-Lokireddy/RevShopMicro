package com.revshop.product.controller;

import com.revshop.product.dto.*;
import com.revshop.product.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Seller Product Controller - Kavya
 * Handles seller dashboard operations for managing products
 */
@RestController
@RequestMapping("/api/seller/products")
public class SellerProductController {

    private static final Logger log = LoggerFactory.getLogger(SellerProductController.class);

    private final ProductService productService;

    public SellerProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> addProduct(
            @Valid @RequestBody ProductRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /api/seller/products - seller: {}", userId);
        ProductResponse product = productService.addProduct(request, userId);
        return new ResponseEntity<>(product, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("PUT /api/seller/products/{} - seller: {}", id, userId);
        ProductResponse product = productService.updateProduct(id, request, userId);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("DELETE /api/seller/products/{} - seller: {}", id, userId);
        productService.deleteProduct(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<SellerProductResponse>> getSellerProducts(
            @RequestHeader("X-User-Id") Long userId) {
        log.info("GET /api/seller/products - seller: {}", userId);
        List<SellerProductResponse> products = productService.getSellerProducts(userId);
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{id}/threshold")
    public ResponseEntity<ProductResponse> setStockThreshold(
            @PathVariable Long id,
            @Valid @RequestBody ThresholdRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("PUT /api/seller/products/{}/threshold - seller: {}", id, userId);
        ProductResponse product = productService.setStockThreshold(id, request, userId);
        return ResponseEntity.ok(product);
    }
}
