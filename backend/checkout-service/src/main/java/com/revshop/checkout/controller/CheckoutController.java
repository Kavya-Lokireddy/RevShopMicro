package com.revshop.checkout.controller;

import com.revshop.checkout.dto.AddressRequest;
import com.revshop.checkout.dto.CheckoutResponse;
import com.revshop.checkout.dto.InitiateCheckoutRequest;
import com.revshop.checkout.service.CheckoutService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private static final Logger log = LoggerFactory.getLogger(CheckoutController.class);
    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/initiate")
    public ResponseEntity<CheckoutResponse> initiateCheckout(@Valid @RequestBody InitiateCheckoutRequest request) {
        log.info("Received request to initiate checkout for user: {}", request.getUserId());
        CheckoutResponse response = checkoutService.initiateCheckout(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{sessionId}/address")
    public ResponseEntity<CheckoutResponse> addAddress(
            @PathVariable Long sessionId,
            @RequestParam Long userId,
            @Valid @RequestBody AddressRequest request) {
        log.info("Received request to add address to checkout session: {} for user: {}", sessionId, userId);
        CheckoutResponse response = checkoutService.addAddress(sessionId, userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<CheckoutResponse> getCheckoutSession(
            @PathVariable Long sessionId,
            @RequestParam Long userId) {
        log.info("Received request to fetch checkout session: {} for user: {}", sessionId, userId);
        CheckoutResponse response = checkoutService.getCheckoutSession(sessionId, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> cancelCheckout(
            @PathVariable Long sessionId,
            @RequestParam Long userId) {
        log.info("Received request to cancel checkout session: {} for user: {}", sessionId, userId);
        checkoutService.cancelCheckout(sessionId, userId);
        return ResponseEntity.noContent().build();
    }
}
