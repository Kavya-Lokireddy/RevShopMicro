package com.revshop.order.controller;

import com.revshop.order.dto.ProductReviewsResponse;
import com.revshop.order.dto.ReviewRequest;
import com.revshop.order.dto.ReviewResponse;
import com.revshop.order.service.ReviewService;
import com.revshop.order.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> addReview(
            @Valid @RequestBody ReviewRequest request,
            HttpServletRequest httpRequest) {
        Long userId = JwtUtil.getUserIdFromRequest(httpRequest);
        ReviewResponse response = reviewService.addReview(userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ProductReviewsResponse> getProductReviews(@PathVariable Long productId) {
        ProductReviewsResponse response = reviewService.getProductReviewsWithAverage(productId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/product/{productId}/average")
    public ResponseEntity<Map<String, Double>> getProductAverageRating(@PathVariable Long productId) {
        Double averageRating = reviewService.getAverageRating(productId);
        return ResponseEntity.ok(Map.of("averageRating", averageRating));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ReviewResponse>> getMyReviews(HttpServletRequest request) {
        Long userId = JwtUtil.getUserIdFromRequest(request);
        List<ReviewResponse> reviews = reviewService.getUserReviews(userId);
        return ResponseEntity.ok(reviews);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteReview(
            @PathVariable Long reviewId,
            HttpServletRequest request) {
        Long userId = JwtUtil.getUserIdFromRequest(request);
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.ok("Review deleted successfully");
    }
}
