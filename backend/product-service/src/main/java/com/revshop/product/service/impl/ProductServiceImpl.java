package com.revshop.product.service.impl;

import com.revshop.product.dto.*;
import com.revshop.product.entity.Category;
import com.revshop.product.entity.Product;
import com.revshop.product.exception.InsufficientStockException;
import com.revshop.product.exception.ResourceNotFoundException;
import com.revshop.product.exception.UnauthorizedException;
import com.revshop.product.repository.CategoryRepository;
import com.revshop.product.repository.ProductRepository;
import com.revshop.product.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    // ========== Seller Operations (Kavya) ==========

    @Override
    @Transactional
    public ProductResponse addProduct(ProductRequest request, Long sellerId) {
        log.info("Adding new product for seller: {}", sellerId);

        // Validate category exists
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setMrp(request.getMrp());
        product.setQuantity(request.getQuantity());
        product.setImageUrl(request.getImageUrl());
        product.setCategoryId(request.getCategoryId());
        product.setSellerId(sellerId);
        product.setActive(true);
        product.setStockThreshold(5);

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with id: {}", savedProduct.getId());

        return mapToProductResponse(savedProduct);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request, Long sellerId) {
        log.info("Updating product {} for seller: {}", id, sellerId);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // Check if product belongs to this seller
        if (!product.getSellerId().equals(sellerId)) {
            throw new UnauthorizedException("You are not authorized to update this product");
        }

        // Validate category exists
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setMrp(request.getMrp());
        product.setQuantity(request.getQuantity());
        product.setImageUrl(request.getImageUrl());
        product.setCategoryId(request.getCategoryId());

        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully: {}", id);

        return mapToProductResponse(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id, Long sellerId) {
        log.info("Soft deleting product {} for seller: {}", id, sellerId);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // Check if product belongs to this seller
        if (!product.getSellerId().equals(sellerId)) {
            throw new UnauthorizedException("You are not authorized to delete this product");
        }

        // Soft delete
        product.setActive(false);
        productRepository.save(product);
        log.info("Product soft deleted successfully: {}", id);
    }

    @Override
    @Transactional
    public ProductResponse setStockThreshold(Long id, ThresholdRequest request, Long sellerId) {
        log.info("Setting stock threshold for product {} to: {}", id, request.getThreshold());

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // Check if product belongs to this seller
        if (!product.getSellerId().equals(sellerId)) {
            throw new UnauthorizedException("You are not authorized to modify this product");
        }

        product.setStockThreshold(request.getThreshold());
        Product updatedProduct = productRepository.save(product);
        log.info("Stock threshold updated successfully for product: {}", id);

        return mapToProductResponse(updatedProduct);
    }

    @Override
    public List<SellerProductResponse> getSellerProducts(Long sellerId) {
        log.info("Fetching all products for seller: {}", sellerId);

        List<Product> products = productRepository.findBySellerIdAndActiveTrue(sellerId);

        return products.stream()
                .map(this::mapToSellerProductResponse)
                .collect(Collectors.toList());
    }

    // ========== Buyer Operations (Jatin) ==========

    @Override
    public ProductSearchResponse getAllProducts(int page, int size) {
        log.info("Fetching all active products - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> productPage = productRepository.findByActiveTrue(pageable);

        return buildSearchResponse(productPage);
    }

    @Override
    public ProductResponse getProductDetails(Long id) {
        log.info("Fetching product details for id: {}", id);

        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        return mapToProductResponse(product);
    }

    @Override
    public ProductSearchResponse getProductsByCategory(Long categoryId, int page, int size) {
        log.info("Fetching products for category: {} - page: {}, size: {}", categoryId, page, size);

        // Validate category exists
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> productPage = productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable);

        return buildSearchResponse(productPage);
    }

    @Override
    public List<ProductResponse> searchProducts(String keyword) {
        log.info("Searching products with keyword: {}", keyword);

        List<Product> products = productRepository.searchByKeyword(keyword);

        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getProductsByIds(List<Long> ids) {
        log.info("Fetching products for ids: {}", ids);

        List<Product> products = productRepository.findAllById(ids);

        return products.stream()
                .filter(Product::getActive)
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    // ========== Internal Operations ==========

    @Override
    public ProductResponse getProductById(Long id) {
        log.info("Internal: Fetching product by id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        return mapToProductResponse(product);
    }

    @Override
    @Transactional
    public void updateStock(Long id, Integer quantity) {
        log.info("Internal: Updating stock for product {} by quantity: {}", id, quantity);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        int newQuantity = product.getQuantity() + quantity;

        if (newQuantity < 0) {
            throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
        }

        product.setQuantity(newQuantity);
        productRepository.save(product);
        log.info("Stock updated successfully for product: {}. New quantity: {}", id, newQuantity);
    }

    @Override
    @Transactional
    public void updateRating(Long id, Double rating) {
        log.info("Internal: Updating rating for product {} to: {}", id, rating);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setRating(rating);
        productRepository.save(product);
    }

    // ========== Category Operations ==========

    @Override
    public List<CategoryResponse> getAllCategories() {
        log.info("Fetching all categories");

        List<Category> categories = categoryRepository.findAll();

        return categories.stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryResponse addCategory(CategoryRequest request) {
        log.info("Adding new category: {}", request.getName());

        if (categoryRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Category already exists with name: " + request.getName());
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with id: {}", savedCategory.getId());

        return mapToCategoryResponse(savedCategory);
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    // ========== Helper Methods ==========

    private ProductResponse mapToProductResponse(Product product) {
        String categoryName = null;
        if (product.getCategory() != null) {
            categoryName = product.getCategory().getName();
        }

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .mrp(product.getMrp())
                .discountPercentage(product.getDiscountPercentage())
                .quantity(product.getQuantity())
                .rating(product.getRating())
                .imageUrl(product.getImageUrl())
                .active(product.getActive())
                .stockThreshold(product.getStockThreshold())
                .sellerId(product.getSellerId())
                .categoryId(product.getCategoryId())
                .categoryName(categoryName)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private SellerProductResponse mapToSellerProductResponse(Product product) {
        String categoryName = null;
        if (product.getCategory() != null) {
            categoryName = product.getCategory().getName();
        }

        return SellerProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .mrp(product.getMrp())
                .discountPercentage(product.getDiscountPercentage())
                .quantity(product.getQuantity())
                .rating(product.getRating())
                .imageUrl(product.getImageUrl())
                .active(product.getActive())
                .stockThreshold(product.getStockThreshold())
                .categoryId(product.getCategoryId())
                .categoryName(categoryName)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .lowStock(product.getQuantity() <= product.getStockThreshold())
                .build();
    }

    private CategoryResponse mapToCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }

    private ProductSearchResponse buildSearchResponse(Page<Product> productPage) {
        List<ProductResponse> products = productPage.getContent().stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());

        return ProductSearchResponse.builder()
                .products(products)
                .currentPage(productPage.getNumber())
                .totalPages(productPage.getTotalPages())
                .totalItems(productPage.getTotalElements())
                .pageSize(productPage.getSize())
                .build();
    }
}
