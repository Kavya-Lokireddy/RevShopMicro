package com.revshop.checkout.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revshop.checkout.client.CartServiceClient;
import com.revshop.checkout.client.OrderServiceClient;
import com.revshop.checkout.dto.CreateOrderRequest;
import com.revshop.checkout.dto.PaymentRequest;
import com.revshop.checkout.dto.PaymentResponse;
import com.revshop.checkout.entity.CheckoutSession;
import com.revshop.checkout.entity.CheckoutStatus;
import com.revshop.checkout.entity.PaymentMethod;
import com.revshop.checkout.entity.PaymentStatus;
import com.revshop.checkout.entity.PaymentTransaction;
import com.revshop.checkout.exception.CheckoutExpiredException;
import com.revshop.checkout.exception.CheckoutNotFoundException;
import com.revshop.checkout.exception.PaymentFailedException;
import com.revshop.checkout.repository.CheckoutSessionRepository;
import com.revshop.checkout.repository.PaymentTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;

    @Mock
    private CheckoutSessionRepository checkoutSessionRepository;

    @Mock
    private OrderServiceClient orderServiceClient;

    @Mock
    private CartServiceClient cartServiceClient;

    private ObjectMapper objectMapper;

    private PaymentServiceImpl paymentService;

    private CheckoutSession testSession;
    private PaymentRequest paymentRequest;
    private Map<String, Object> orderResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        paymentService = new PaymentServiceImpl(
                checkoutSessionRepository,
                paymentTransactionRepository,
                orderServiceClient,
                cartServiceClient,
                objectMapper
        );
        testSession = new CheckoutSession();
        testSession.setId(1L);
        testSession.setUserId(1L);
        testSession.setTotalAmount(59.98);
        testSession.setCartSnapshot("{\"items\":[{\"productId\":1, \"quantity\":2}]}");
        testSession.setStatus(CheckoutStatus.ADDRESS_ADDED);
        testSession.setPaymentStatus(PaymentStatus.PENDING);
        testSession.setShippingAddress("123 Main St");
        testSession.setBillingAddress("123 Main St");
        testSession.setContactName("John Doe");
        testSession.setPhoneNumber("1234567890");
        testSession.setCreatedAt(LocalDateTime.now());
        testSession.setExpiresAt(LocalDateTime.now().plusMinutes(30));

        paymentRequest = new PaymentRequest();
        paymentRequest.setCheckoutSessionId(1L);
        paymentRequest.setPaymentMethod(PaymentMethod.COD);

        orderResponse = new HashMap<>();
        orderResponse.put("id", 100);
    }

    @Test
    @DisplayName("Test processPayment - COD should succeed immediately")
    void testProcessPayment_COD_Success() {
        when(checkoutSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        when(orderServiceClient.createOrder(any(CreateOrderRequest.class))).thenReturn(orderResponse);
        when(paymentTransactionRepository.save(any(PaymentTransaction.class))).thenAnswer(i -> i.getArgument(0));

        PaymentResponse response = paymentService.processPayment(paymentRequest);

        assertNotNull(response);
        assertEquals(PaymentStatus.COMPLETED, response.getStatus());
        assertEquals("Payment completed successfully", response.getMessage());
        assertEquals(PaymentStatus.COMPLETED, testSession.getPaymentStatus());
        assertEquals(CheckoutStatus.COMPLETED, testSession.getStatus());
        verify(checkoutSessionRepository).save(testSession);
        verify(paymentTransactionRepository, atLeastOnce()).save(any(PaymentTransaction.class));
        verify(cartServiceClient).clearCart(1L);
    }

    @Test
    @DisplayName("Test processPayment - CARD should succeed and create transaction")
    void testProcessPayment_Card_Success() {
        paymentRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        PaymentRequest.CardDetails cardDetails = new PaymentRequest.CardDetails();
        cardDetails.setCardNumber("1234567890123456");
        cardDetails.setExpiryDate("12/25");
        cardDetails.setCvv("123");
        paymentRequest.setCardDetails(cardDetails);

        when(checkoutSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        when(orderServiceClient.createOrder(any(CreateOrderRequest.class))).thenReturn(orderResponse);
        when(paymentTransactionRepository.save(any(PaymentTransaction.class))).thenAnswer(i -> i.getArgument(0));

        PaymentResponse response = paymentService.processPayment(paymentRequest);

        assertNotNull(response);
        assertEquals(PaymentStatus.COMPLETED, response.getStatus());
        assertEquals(PaymentStatus.COMPLETED, testSession.getPaymentStatus());
        assertEquals(CheckoutStatus.COMPLETED, testSession.getStatus());
        verify(cartServiceClient).clearCart(1L);
    }

    @Test
    @DisplayName("Test processPayment - throws exception when session not found")
    void testProcessPayment_SessionNotFound() {
        when(checkoutSessionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CheckoutNotFoundException.class, () -> paymentService.processPayment(paymentRequest));
    }

    @Test
    @DisplayName("Test processPayment - throws exception for expired session")
    void testProcessPayment_ExpiredSession() {
        testSession.setExpiresAt(LocalDateTime.now().minusMinutes(10));
        when(checkoutSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));

        assertThrows(CheckoutExpiredException.class, () -> paymentService.processPayment(paymentRequest));
    }

    @Test
    @DisplayName("Test processPayment - throws exception if already completed")
    void testProcessPayment_AlreadyCompleted() {
        testSession.setStatus(CheckoutStatus.COMPLETED);
        when(checkoutSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));

        assertThrows(IllegalArgumentException.class, () -> paymentService.processPayment(paymentRequest));
    }

    @Test
    @DisplayName("Test processPayment - order creation failure rolls back payment")
    void testProcessPayment_OrderCreationFails() {
        when(checkoutSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        when(orderServiceClient.createOrder(any(CreateOrderRequest.class))).thenThrow(new RuntimeException("Order API Down"));

        assertThrows(PaymentFailedException.class, () -> paymentService.processPayment(paymentRequest));
        
        // Transaction should be saved with FAILED status
        verify(paymentTransactionRepository, atLeastOnce()).save(argThat(t -> t.getStatus() == PaymentStatus.FAILED));
        // Cart should not be cleared
        verify(cartServiceClient, never()).clearCart(anyLong());
    }

    @Test
    @DisplayName("Test getTransactionById - should return transaction")
    void testGetTransactionById_Success() {
        PaymentTransaction pt = new PaymentTransaction();
        when(paymentTransactionRepository.findById(1L)).thenReturn(Optional.of(pt));

        PaymentTransaction result = paymentService.getTransactionById(1L);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Test getTransactionByTransactionId - should return transaction by string ID")
    void testGetTransactionByTransactionId_Success() {
        PaymentTransaction pt = new PaymentTransaction();
        when(paymentTransactionRepository.findByTransactionId("txn_123")).thenReturn(Optional.of(pt));

        PaymentTransaction result = paymentService.getTransactionByTransactionId("txn_123");

        assertNotNull(result);
    }
}
