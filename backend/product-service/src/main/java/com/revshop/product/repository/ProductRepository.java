package com.revshop.product.repository;

import com.revshop.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find active products by seller ID (for the seller dashboard)
    List<Product> findBySellerIdAndActiveTrue(Long sellerId);

    // Find by category ID with pagination (for Jatin's buyer view)
    Page<Product> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);

    // Find all active products (for Jatin's buyer view)
    Page<Product> findByActiveTrue(Pageable pageable);

    // Find active product by ID
    Optional<Product> findByIdAndActiveTrue(Long id);

    // Search products by keyword (name or description) - active only
    @Query("SELECT p FROM Product p WHERE " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND p.active = true")
    List<Product> searchByKeyword(@Param("keyword") String keyword);

    // Check if product exists for seller (for authorization)
    boolean existsByIdAndSellerId(Long id, Long sellerId);
}
