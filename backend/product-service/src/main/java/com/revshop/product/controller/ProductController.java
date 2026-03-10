package com.revshop.product.controller;

import com.revshop.product.dto.ProductResponse;
import com.revshop.product.dto.ProductSearchResponse;
import com.revshop.product.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Buyer Product Controller - Jatin
 * Handles all buyer-facing product operations
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<ProductSearchResponse> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/products - page: {}, size: {}", page, size);
        ProductSearchResponse response = productService.getAllProducts(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductDetails(@PathVariable Long id) {
        log.info("GET /api/products/{}", id);
        ProductResponse product = productService.getProductDetails(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ProductSearchResponse> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/products/category/{} - page: {}, size: {}", categoryId, page, size);
        ProductSearchResponse response = productService.getProductsByCategory(categoryId, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam String keyword) {
        log.info("GET /api/products/search?keyword={}", keyword);
        List<ProductResponse> products = productService.searchProducts(keyword);
        return ResponseEntity.ok(products);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<ProductResponse>> getProductsBatch(@RequestBody List<Long> ids) {
        log.info("POST /api/products/batch for ids: {}", ids);
        List<ProductResponse> products = productService.getProductsByIds(ids);
        return ResponseEntity.ok(products);
    }
}
