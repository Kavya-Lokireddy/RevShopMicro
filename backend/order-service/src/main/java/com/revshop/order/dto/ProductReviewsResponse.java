package com.revshop.order.dto;

import java.util.List;

public class ProductReviewsResponse {

    private List<ReviewResponse> reviews;
    private Double averageRating;
    private Long totalReviews;

    public ProductReviewsResponse() {
    }

    public ProductReviewsResponse(List<ReviewResponse> reviews, Double averageRating, Long totalReviews) {
        this.reviews = reviews;
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
    }

    public List<ReviewResponse> getReviews() {
        return reviews;
    }

    public void setReviews(List<ReviewResponse> reviews) {
        this.reviews = reviews;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Long getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Long totalReviews) {
        this.totalReviews = totalReviews;
    }
}
