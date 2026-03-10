package com.revshop.order.service;

import com.revshop.order.dto.ProductReviewsResponse;
import com.revshop.order.dto.ReviewRequest;
import com.revshop.order.dto.ReviewResponse;

import java.util.List;

public interface ReviewService {

    ReviewResponse addReview(Long userId, ReviewRequest request);

    List<ReviewResponse> getProductReviews(Long productId);

    List<ReviewResponse> getUserReviews(Long userId);

    ProductReviewsResponse getProductReviewsWithAverage(Long productId);

    Double getAverageRating(Long productId);

    void deleteReview(Long reviewId, Long userId);
}
