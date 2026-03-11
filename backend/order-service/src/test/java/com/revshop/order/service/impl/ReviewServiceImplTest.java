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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ProductServiceClient productServiceClient;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private Order testOrder;
    private Review testReview;
    private ReviewRequest reviewRequest;

    @BeforeEach
    void setUp() {
        // Setup order item
        OrderItem orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setProductId(1L);
        orderItem.setProductName("Test Product");
        orderItem.setSellerId(10L);
        orderItem.setQuantity(2);
        orderItem.setPriceAtPurchase(29.99);
        orderItem.setSubtotal(59.98);

        // Setup test order
        testOrder = new Order();
        testOrder.setId(100L);
        testOrder.setUserId(1L);
        testOrder.setTotalAmount(59.98);
        testOrder.setShippingAddress("123 Main St");
        testOrder.setBillingAddress("123 Main St");
        testOrder.setContactName("John Doe");
        testOrder.setPhoneNumber("1234567890");
        testOrder.setPaymentMethod("COD");
        testOrder.setPaymentStatus("COMPLETED");
        testOrder.setOrderDate(LocalDateTime.now());
        testOrder.setStatus(OrderStatus.DELIVERED);
        testOrder.setCreatedAt(LocalDateTime.now());
        testOrder.setOrderItems(new ArrayList<>(List.of(orderItem)));
        orderItem.setOrder(testOrder);

        // Setup review request
        reviewRequest = new ReviewRequest();
        reviewRequest.setProductId(1L);
        reviewRequest.setOrderId(100L);
        reviewRequest.setRating(5);
        reviewRequest.setComment("Great product!");

        // Setup test review
        testReview = new Review();
        testReview.setId(1L);
        testReview.setUserId(1L);
        testReview.setProductId(1L);
        testReview.setOrderId(100L);
        testReview.setRating(5);
        testReview.setComment("Great product!");
        testReview.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Test addReview - should add review successfully")
    void testAddReview_Success() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(testOrder));
        when(reviewRepository.existsByUserIdAndProductIdAndOrderId(1L, 1L, 100L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
        when(reviewRepository.findAverageRatingByProductId(1L)).thenReturn(5.0);

        ReviewResponse response = reviewService.addReview(1L, reviewRequest);

        assertNotNull(response);
        assertEquals(5, response.getRating());
        assertEquals("Great product!", response.getComment());
        assertEquals(1L, response.getProductId());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @DisplayName("Test addReview - should throw exception when order not found")
    void testAddReview_OrderNotFound() {
        when(orderRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> reviewService.addReview(1L, reviewRequest));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("Test addReview - should throw exception when not own order")
    void testAddReview_UnauthorizedUser() {
        testOrder.setUserId(999L); // Different user
        when(orderRepository.findById(100L)).thenReturn(Optional.of(testOrder));

        assertThrows(UnauthorizedException.class, () -> reviewService.addReview(1L, reviewRequest));
    }

    @Test
    @DisplayName("Test addReview - should throw exception for cancelled order")
    void testAddReview_CancelledOrder() {
        testOrder.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(testOrder));

        assertThrows(ReviewNotAllowedException.class, () -> reviewService.addReview(1L, reviewRequest));
    }

    @Test
    @DisplayName("Test addReview - should throw exception for duplicate review")
    void testAddReview_DuplicateReview() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(testOrder));
        when(reviewRepository.existsByUserIdAndProductIdAndOrderId(1L, 1L, 100L)).thenReturn(true);

        assertThrows(ReviewNotAllowedException.class, () -> reviewService.addReview(1L, reviewRequest));
    }

    @Test
    @DisplayName("Test getProductReviews - should return all product reviews")
    void testGetProductReviews_Success() {
        when(reviewRepository.findByProductIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(testReview));

        List<ReviewResponse> responses = reviewService.getProductReviews(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(5, responses.get(0).getRating());
    }

    @Test
    @DisplayName("Test getUserReviews - should return all user reviews")
    void testGetUserReviews_Success() {
        when(reviewRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(testReview));

        List<ReviewResponse> responses = reviewService.getUserReviews(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).getUserId());
    }

    @Test
    @DisplayName("Test getProductReviewsWithAverage - should return reviews with average")
    void testGetProductReviewsWithAverage_Success() {
        when(reviewRepository.findByProductIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(testReview));
        when(reviewRepository.findAverageRatingByProductId(1L)).thenReturn(4.5);
        when(reviewRepository.countByProductId(1L)).thenReturn(3L);

        ProductReviewsResponse response = reviewService.getProductReviewsWithAverage(1L);

        assertNotNull(response);
        assertEquals(4.5, response.getAverageRating());
        assertEquals(3L, response.getTotalReviews());
        assertEquals(1, response.getReviews().size());
    }

    @Test
    @DisplayName("Test getAverageRating - should return zero when no reviews")
    void testGetAverageRating_NoReviews() {
        when(reviewRepository.findAverageRatingByProductId(999L)).thenReturn(null);

        Double average = reviewService.getAverageRating(999L);

        assertEquals(0.0, average);
    }

    @Test
    @DisplayName("Test deleteReview - should delete review successfully")
    void testDeleteReview_Success() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(reviewRepository.findAverageRatingByProductId(1L)).thenReturn(4.0);

        reviewService.deleteReview(1L, 1L);

        verify(reviewRepository).delete(testReview);
    }

    @Test
    @DisplayName("Test deleteReview - should throw exception when deleting another user's review")
    void testDeleteReview_Unauthorized() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

        assertThrows(UnauthorizedException.class, () -> reviewService.deleteReview(1L, 999L));
        verify(reviewRepository, never()).delete(any(Review.class));
    }
}
