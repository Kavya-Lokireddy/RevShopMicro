package com.revshop.checkout.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revshop.checkout.client.CartServiceClient;
import com.revshop.checkout.dto.AddressRequest;
import com.revshop.checkout.dto.CartDto;
import com.revshop.checkout.dto.CheckoutResponse;
import com.revshop.checkout.dto.InitiateCheckoutRequest;
import com.revshop.checkout.entity.CheckoutSession;
import com.revshop.checkout.entity.CheckoutStatus;
import com.revshop.checkout.exception.CheckoutExpiredException;
import com.revshop.checkout.exception.CheckoutNotFoundException;
import com.revshop.checkout.repository.CheckoutSessionRepository;
import com.revshop.checkout.service.CheckoutService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class CheckoutServiceImpl implements CheckoutService {

    private static final Logger log = LoggerFactory.getLogger(CheckoutServiceImpl.class);
    private final CheckoutSessionRepository checkoutSessionRepository;
    private final CartServiceClient cartServiceClient;
    private final ObjectMapper objectMapper;

    public CheckoutServiceImpl(CheckoutSessionRepository checkoutSessionRepository,
                               CartServiceClient cartServiceClient,
                               ObjectMapper objectMapper) {
        this.checkoutSessionRepository = checkoutSessionRepository;
        this.cartServiceClient = cartServiceClient;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "cartService", fallbackMethod = "initiateCheckoutFallback")
    public CheckoutResponse initiateCheckout(InitiateCheckoutRequest request) {
        log.info("Initiating checkout for user: {}", request.getUserId());

        // Fetch cart from cart-service
        CartDto cart = cartServiceClient.getCart(request.getUserId());

        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty. Cannot initiate checkout.");
        }

        // Create checkout session
        CheckoutSession session = new CheckoutSession();
        session.setUserId(request.getUserId());
        session.setTotalAmount(cart.getTotalAmount());
        session.setStatus(CheckoutStatus.INITIATED);

        // Save cart snapshot as JSON
        try {
            String cartSnapshot = objectMapper.writeValueAsString(cart);
            session.setCartSnapshot(cartSnapshot);
        } catch (JsonProcessingException e) {
            log.error("Error serializing cart snapshot", e);
            throw new RuntimeException("Failed to create checkout session", e);
        }

        session = checkoutSessionRepository.save(session);
        log.info("Checkout session created with ID: {}", session.getId());

        return mapToResponse(session);
    }

    @Override
    @Transactional
    public CheckoutResponse addAddress(Long sessionId, Long userId, AddressRequest request) {
        log.info("Adding address to checkout session: {}", sessionId);

        CheckoutSession session = checkoutSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new CheckoutNotFoundException("Checkout session not found"));

        if (session.isExpired()) {
            session.setStatus(CheckoutStatus.EXPIRED);
            checkoutSessionRepository.save(session);
            throw new CheckoutExpiredException("Checkout session has expired");
        }

        session.setShippingAddress(request.getShippingAddress());
        session.setBillingAddress(request.getBillingAddress() != null ?
                request.getBillingAddress() : request.getShippingAddress());
        session.setContactName(request.getContactName());
        session.setPhoneNumber(request.getPhoneNumber());
        session.setStatus(CheckoutStatus.ADDRESS_ADDED);

        session = checkoutSessionRepository.save(session);
        log.info("Address added to checkout session: {}", sessionId);

        return mapToResponse(session);
    }

    @Override
    @Transactional
    public CheckoutResponse getCheckoutSession(Long sessionId, Long userId) {
        log.info("Fetching checkout session: {} for user: {}", sessionId, userId);

        CheckoutSession session = checkoutSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new CheckoutNotFoundException("Checkout session not found"));

        if (session.isExpired() && session.getStatus() != CheckoutStatus.COMPLETED) {
            session.setStatus(CheckoutStatus.EXPIRED);
            checkoutSessionRepository.save(session);
        }

        return mapToResponse(session);
    }

    @Override
    @Transactional
    public void cancelCheckout(Long sessionId, Long userId) {
        log.info("Cancelling checkout session: {} for user: {}", sessionId, userId);

        CheckoutSession session = checkoutSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new CheckoutNotFoundException("Checkout session not found"));

        if (session.getStatus() == CheckoutStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot cancel a completed checkout");
        }

        session.setStatus(CheckoutStatus.CANCELLED);
        checkoutSessionRepository.save(session);
        log.info("Checkout session cancelled: {}", sessionId);
    }

    private CheckoutResponse mapToResponse(CheckoutSession session) {
        CheckoutResponse response = new CheckoutResponse();
        response.setSessionId(session.getId());
        response.setUserId(session.getUserId());
        response.setCartSnapshot(session.getCartSnapshot());
        response.setShippingAddress(session.getShippingAddress());
        response.setBillingAddress(session.getBillingAddress());
        response.setContactName(session.getContactName());
        response.setPhoneNumber(session.getPhoneNumber());
        response.setPaymentMethod(session.getPaymentMethod());
        response.setPaymentStatus(session.getPaymentStatus());
        response.setTotalAmount(session.getTotalAmount());
        response.setStatus(session.getStatus());
        response.setCreatedAt(session.getCreatedAt());
        response.setExpiresAt(session.getExpiresAt());
        response.setExpired(session.isExpired());
        return response;
    }

    // Fallback method for circuit breaker
    public CheckoutResponse initiateCheckoutFallback(InitiateCheckoutRequest request, Exception e) {
        log.error("Cart service is unavailable. Fallback triggered.", e);
        throw new RuntimeException("Cart service is currently unavailable. Please try again later.");
    }
}
