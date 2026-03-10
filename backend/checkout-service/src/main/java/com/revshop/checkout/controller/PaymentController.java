package com.revshop.checkout.controller;

import com.revshop.checkout.dto.PaymentRequest;
import com.revshop.checkout.dto.PaymentResponse;
import com.revshop.checkout.entity.PaymentTransaction;
import com.revshop.checkout.service.PaymentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        log.info("Received request to process payment for checkout session: {}", request.getCheckoutSessionId());
        PaymentResponse response = paymentService.processPayment(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<PaymentTransaction> getTransactionStatus(@PathVariable String transactionId) {
        log.info("Received request to fetch transaction status: {}", transactionId);
        PaymentTransaction transaction = paymentService.getTransactionByTransactionId(transactionId);
        return ResponseEntity.ok(transaction);
    }
}
