package com.revshop.order.service.impl;

import com.revshop.order.client.ProductServiceClient;
import com.revshop.order.dto.ProductReviewsResponse;
import com.revshop.order.dto.ReviewRequest;
import com.revshop.order.dto.ReviewResponse;
import com.revshop.order.entity.*;
import com.revshop.order.exception.OrderNotFoundException;
import com.revshop.order.exception.ReviewNotAllowedException;
import com.revshop.order.exception.UnauthorizedException;
import com.revshop.order.repository.OrderRepository;
import com.revshop.order.repository.ReviewRepository;
import com.revshop.order.service.NotificationService;
import com.revshop.order.service.ReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;
    private final ProductServiceClient productServiceClient;

    public ReviewServiceImpl(ReviewRepository reviewRepository, OrderRepository orderRepository, NotificationService notificationService, ProductServiceClient productServiceClient) {
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
        this.notificationService = notificationService;
        this.productServiceClient = productServiceClient;
    }

    @Override
    @Transactional
    public ReviewResponse addReview(Long userId, ReviewRequest request) {
        // Verify that the user purchased this product in this order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + request.getOrderId()));

        if (!order.getUserId().equals(userId)) {
            throw new UnauthorizedException("You can only review your own purchases");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new ReviewNotAllowedException("Cannot review a cancelled order");
        }

        // Check if the product is in this order
        boolean productInOrder = order.getOrderItems().stream()
                .anyMatch(item -> item.getProductId().equals(request.getProductId()));

        if (!productInOrder) {
            throw new ReviewNotAllowedException("You can only review products you have purchased");
        }

        // Check for duplicate review
        if (reviewRepository.existsByUserIdAndProductIdAndOrderId(userId, request.getProductId(), request.getOrderId())) {
            throw new ReviewNotAllowedException("You have already reviewed this product for this order");
        }

        Review review = new Review();
        review.setUserId(userId);
        review.setProductId(request.getProductId());
        review.setOrderId(request.getOrderId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review savedReview = reviewRepository.save(review);

        // Update product rating via Feign client
        Double averageRating = getAverageRating(request.getProductId());
        try {
            productServiceClient.updateProductRating(request.getProductId(), averageRating);
        } catch (Exception e) {
            // Log error but don't fail the review creation
            System.err.println("Failed to update product rating: " + e.getMessage());
        }

        // Notify seller about the new review
        OrderItem orderItem = order.getOrderItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst()
                .orElse(null);

        if (orderItem != null) {
            notificationService.createNotification(
                    orderItem.getSellerId(),
                    "New " + request.getRating() + "-star review for product: " + orderItem.getProductName(),
                    NotificationType.NEW_REVIEW,
                    savedReview.getId()
            );
        }

        return mapToReviewResponse(savedReview);
    }

    @Override
    public List<ReviewResponse> getProductReviews(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(this::mapToReviewResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewResponse> getUserReviews(Long userId) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToReviewResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProductReviewsResponse getProductReviewsWithAverage(Long productId) {
        List<ReviewResponse> reviews = getProductReviews(productId);
        Double averageRating = getAverageRating(productId);
        Long totalReviews = reviewRepository.countByProductId(productId);

        return new ProductReviewsResponse(reviews, averageRating, totalReviews);
    }

    @Override
    public Double getAverageRating(Long productId) {
        Double average = reviewRepository.findAverageRatingByProductId(productId);
        return average != null ? Math.round(average * 10.0) / 10.0 : 0.0;
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new OrderNotFoundException("Review not found with id: " + reviewId));

        if (!review.getUserId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own reviews");
        }

        Long productId = review.getProductId();
        reviewRepository.delete(review);

        // Update product rating after deletion
        Double averageRating = getAverageRating(productId);
        try {
            productServiceClient.updateProductRating(productId, averageRating);
        } catch (Exception e) {
            System.err.println("Failed to update product rating: " + e.getMessage());
        }
    }

    private ReviewResponse mapToReviewResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getUserId(),
                review.getProductId(),
                review.getOrderId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}
