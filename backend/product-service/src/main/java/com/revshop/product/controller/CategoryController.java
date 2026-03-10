package com.revshop.product.controller;

import com.revshop.product.dto.CategoryRequest;
import com.revshop.product.dto.CategoryResponse;
import com.revshop.product.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Category Controller
 * Handles category operations
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private static final Logger log = LoggerFactory.getLogger(CategoryController.class);

    private final ProductService productService;

    public CategoryController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        log.info("GET /api/categories");
        List<CategoryResponse> categories = productService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> addCategory(
            @Valid @RequestBody CategoryRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        log.info("POST /api/categories - role: {}", role);

        // In a real system, you'd check if the role is "ADMIN"
        // For now, we'll allow it without strict role checking
        CategoryResponse category = productService.addCategory(request);
        return new ResponseEntity<>(category, HttpStatus.CREATED);
    }
}
