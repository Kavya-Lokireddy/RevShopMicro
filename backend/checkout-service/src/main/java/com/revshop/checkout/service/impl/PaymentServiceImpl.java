package com.revshop.checkout.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revshop.checkout.client.CartServiceClient;
import com.revshop.checkout.client.OrderServiceClient;
import com.revshop.checkout.dto.*;
import com.revshop.checkout.entity.*;
import com.revshop.checkout.exception.CheckoutExpiredException;
import com.revshop.checkout.exception.CheckoutNotFoundException;
import com.revshop.checkout.exception.PaymentFailedException;
import com.revshop.checkout.repository.CheckoutSessionRepository;
import com.revshop.checkout.repository.PaymentTransactionRepository;
import com.revshop.checkout.service.PaymentService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);
    private final CheckoutSessionRepository checkoutSessionRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OrderServiceClient orderServiceClient;
    private final CartServiceClient cartServiceClient;
    private final ObjectMapper objectMapper;

    public PaymentServiceImpl(CheckoutSessionRepository checkoutSessionRepository,
                              PaymentTransactionRepository paymentTransactionRepository,
                              OrderServiceClient orderServiceClient,
                              CartServiceClient cartServiceClient,
                              ObjectMapper objectMapper) {
        this.checkoutSessionRepository = checkoutSessionRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.orderServiceClient = orderServiceClient;
        this.cartServiceClient = cartServiceClient;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "orderService", fallbackMethod = "processPaymentFallback")
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for checkout session: {}", request.getCheckoutSessionId());

        // Fetch checkout session
        CheckoutSession session = checkoutSessionRepository.findById(request.getCheckoutSessionId())
                .orElseThrow(() -> new CheckoutNotFoundException("Checkout session not found"));

        // Validate session
        validateCheckoutSession(session);

        // Create payment transaction
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setCheckoutSessionId(session.getId());
        transaction.setAmount(session.getTotalAmount());
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setTransactionId(generateTransactionId());
        transaction.setStatus(PaymentStatus.PENDING);

        try {
            // Validate payment
            validatePayment(request);

            // Process payment based on method
            boolean paymentSuccess = false;
            if (request.getPaymentMethod() == PaymentMethod.COD) {
                paymentSuccess = processCODPayment(session);
            } else {
                paymentSuccess = processCardPayment(request, session);
            }

            if (!paymentSuccess) {
                transaction.setStatus(PaymentStatus.FAILED);
                transaction.setErrorMessage("Payment processing failed");
                paymentTransactionRepository.save(transaction);
                throw new PaymentFailedException("Payment processing failed");
            }

            // Update transaction status
            transaction.setStatus(PaymentStatus.COMPLETED);
            session.setPaymentMethod(request.getPaymentMethod());
            session.setPaymentStatus(PaymentStatus.COMPLETED);

            // Create order in order-service
            Long orderId = createOrder(session);
            transaction.setOrderId(orderId);

            // Update checkout session
            session.setStatus(CheckoutStatus.COMPLETED);
            checkoutSessionRepository.save(session);

            // Clear cart
            try {
                cartServiceClient.clearCart(session.getUserId());
                log.info("Cart cleared for user: {}", session.getUserId());
            } catch (Exception e) {
                log.warn("Failed to clear cart for user: {}", session.getUserId(), e);
            }

            // Save transaction
            transaction = paymentTransactionRepository.save(transaction);
            log.info("Payment processed successfully. Transaction ID: {}", transaction.getTransactionId());

            return new PaymentResponse(
                    transaction.getTransactionId(),
                    PaymentStatus.COMPLETED,
                    orderId,
                    "Payment completed successfully"
            );

        } catch (Exception e) {
            log.error("Payment processing failed", e);
            transaction.setStatus(PaymentStatus.FAILED);
            transaction.setErrorMessage(e.getMessage());
            paymentTransactionRepository.save(transaction);
            throw new PaymentFailedException("Payment failed: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentTransaction getTransactionById(Long id) {
        return paymentTransactionRepository.findById(id)
                .orElseThrow(() -> new CheckoutNotFoundException("Transaction not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentTransaction getTransactionByTransactionId(String transactionId) {
        return paymentTransactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new CheckoutNotFoundException("Transaction not found"));
    }

    private void validateCheckoutSession(CheckoutSession session) {
        if (session.isExpired()) {
            throw new CheckoutExpiredException("Checkout session has expired");
        }

        if (session.getStatus() == CheckoutStatus.COMPLETED) {
            throw new IllegalArgumentException("Payment already completed for this session");
        }

        if (session.getStatus() == CheckoutStatus.CANCELLED) {
            throw new IllegalArgumentException("Checkout session has been cancelled");
        }

        if (session.getShippingAddress() == null || session.getShippingAddress().isEmpty()) {
            throw new IllegalArgumentException("Shipping address is required before payment");
        }
    }

    private void validatePayment(PaymentRequest request) {
        if (request.getPaymentMethod() != PaymentMethod.COD) {
            if (request.getCardDetails() == null) {
                throw new IllegalArgumentException("Card details are required for card payments");
            }
            // Additional card validation can be added here
        }
    }

    private boolean processCODPayment(CheckoutSession session) {
        log.info("Processing COD payment for session: {}", session.getId());
        // COD payment is always accepted
        return true;
    }

    private boolean processCardPayment(PaymentRequest request, CheckoutSession session) {
        log.info("Processing card payment for session: {}", session.getId());
        // Simulate card payment processing
        // In a real system, this would integrate with a payment gateway
        PaymentRequest.CardDetails cardDetails = request.getCardDetails();

        if (cardDetails == null || cardDetails.getCardNumber() == null) {
            return false;
        }

        // Simple validation (in real system, use payment gateway)
        if (cardDetails.getCardNumber().length() < 13 || cardDetails.getCardNumber().length() > 19) {
            throw new PaymentFailedException("Invalid card number");
        }

        // Simulate payment gateway response
        return true;
    }

    private Long createOrder(CheckoutSession session) {
        log.info("Creating order for checkout session: {}", session.getId());

        // Parse cart snapshot
        CartDto cart;
        try {
            cart = objectMapper.readValue(session.getCartSnapshot(), CartDto.class);
        } catch (JsonProcessingException e) {
            log.error("Error parsing cart snapshot", e);
            throw new RuntimeException("Failed to parse cart data", e);
        }

        // Build order request
        CreateOrderRequest orderRequest = new CreateOrderRequest();
        orderRequest.setUserId(session.getUserId());
        orderRequest.setShippingAddress(session.getShippingAddress());
        orderRequest.setBillingAddress(session.getBillingAddress());
        orderRequest.setContactName(session.getContactName());
        orderRequest.setPhoneNumber(session.getPhoneNumber());
        orderRequest.setPaymentMethod(session.getPaymentMethod().name());
        orderRequest.setPaymentStatus(session.getPaymentStatus().name());
        orderRequest.setTotalAmount(session.getTotalAmount());

        // Convert cart items to order items
        List<CreateOrderRequest.OrderItemRequest> orderItems = cart.getItems().stream()
                .map(item -> new CreateOrderRequest.OrderItemRequest(
                        item.getProductId(),
                        item.getProductName(),
                        item.getSellerId(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());
        orderRequest.setItems(orderItems);

        // Call order service
        Map<String, Object> orderResponse = orderServiceClient.createOrder(orderRequest);

        // Extract order ID from response
        Object orderIdObj = orderResponse.get("orderId");
        if (orderIdObj == null) {
            orderIdObj = orderResponse.get("id");
        }

        if (orderIdObj == null) {
            throw new RuntimeException("Order service did not return order ID");
        }

        return orderIdObj instanceof Number ? ((Number) orderIdObj).longValue() : Long.parseLong(orderIdObj.toString());
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString();
    }

    // Fallback method for circuit breaker
    public PaymentResponse processPaymentFallback(PaymentRequest request, Exception e) {
        log.error("Order service is unavailable. Fallback triggered.", e);
        throw new PaymentFailedException("Order service is currently unavailable. Please try again later.");
    }
}
