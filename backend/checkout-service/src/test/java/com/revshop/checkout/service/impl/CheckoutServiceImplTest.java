package com.revshop.checkout.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revshop.checkout.client.CartServiceClient;
import com.revshop.checkout.dto.AddressRequest;
import com.revshop.checkout.dto.CartDto;
import com.revshop.checkout.dto.CheckoutResponse;
import com.revshop.checkout.dto.InitiateCheckoutRequest;
import com.revshop.checkout.entity.CheckoutSession;
import com.revshop.checkout.entity.CheckoutStatus;
import com.revshop.checkout.entity.PaymentStatus;
import com.revshop.checkout.exception.CheckoutExpiredException;
import com.revshop.checkout.exception.CheckoutNotFoundException;
import com.revshop.checkout.repository.CheckoutSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceImplTest {

    @Mock
    private CheckoutSessionRepository checkoutSessionRepository;

    @Mock
    private CartServiceClient cartServiceClient;

    private ObjectMapper objectMapper;

    private CheckoutServiceImpl checkoutService;

    private CheckoutSession testSession;
    private CartDto testCart;
    private InitiateCheckoutRequest initiateRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        checkoutService = new CheckoutServiceImpl(checkoutSessionRepository, cartServiceClient, objectMapper);
        // Setup cart DTO
        CartDto.CartItemDto cartItem = new CartDto.CartItemDto();
        cartItem.setProductId(1L);
        cartItem.setProductName("Test Product");
        cartItem.setPrice(29.99);
        cartItem.setQuantity(2);
        cartItem.setSellerId(10L);

        testCart = new CartDto();
        testCart.setItems(List.of(cartItem));
        testCart.setTotalAmount(59.98);

        // Setup initiate request
        initiateRequest = new InitiateCheckoutRequest();
        initiateRequest.setUserId(1L);

        // Setup test checkout session
        testSession = new CheckoutSession();
        testSession.setId(1L);
        testSession.setUserId(1L);
        testSession.setTotalAmount(59.98);
        testSession.setCartSnapshot("{\"items\":[]}");
        testSession.setStatus(CheckoutStatus.INITIATED);
        testSession.setPaymentStatus(PaymentStatus.PENDING);
        testSession.setCreatedAt(LocalDateTime.now());
        testSession.setExpiresAt(LocalDateTime.now().plusMinutes(30));
    }

    @Test
    @DisplayName("Test initiateCheckout - should create checkout session")
    void testInitiateCheckout_Success() throws Exception {
        when(cartServiceClient.getCart(1L)).thenReturn(testCart);

        // Capture saved session to verify cart snapshot
        when(checkoutSessionRepository.save(any(CheckoutSession.class))).thenAnswer(invocation -> {
            CheckoutSession session = invocation.getArgument(0);
            session.setId(1L);
            session.setStatus(CheckoutStatus.INITIATED);
            session.setExpiresAt(LocalDateTime.now().plusMinutes(30));
            return session;
        });

        CheckoutResponse response = checkoutService.initiateCheckout(initiateRequest);

        assertNotNull(response);
        assertEquals(1L, response.getSessionId());
        assertEquals(1L, response.getUserId());
        assertEquals(CheckoutStatus.INITIATED, response.getStatus());
        verify(checkoutSessionRepository).save(any(CheckoutSession.class));
    }

    @Test
    @DisplayName("Test initiateCheckout - should throw exception for empty cart")
    void testInitiateCheckout_EmptyCart() {
        CartDto emptyCart = new CartDto();
        emptyCart.setItems(List.of());
        when(cartServiceClient.getCart(1L)).thenReturn(emptyCart);

        assertThrows(IllegalArgumentException.class, () -> checkoutService.initiateCheckout(initiateRequest));
    }

    @Test
    @DisplayName("Test addAddress - should add address to session")
    void testAddAddress_Success() {
        AddressRequest addressRequest = new AddressRequest();
        addressRequest.setShippingAddress("123 Main St");
        addressRequest.setBillingAddress("123 Main St");
        addressRequest.setContactName("John Doe");
        addressRequest.setPhoneNumber("1234567890");

        when(checkoutSessionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testSession));
        when(checkoutSessionRepository.save(any(CheckoutSession.class))).thenReturn(testSession);

        CheckoutResponse response = checkoutService.addAddress(1L, 1L, addressRequest);

        assertNotNull(response);
        assertEquals("123 Main St", testSession.getShippingAddress());
        assertEquals(CheckoutStatus.ADDRESS_ADDED, testSession.getStatus());
    }

    @Test
    @DisplayName("Test addAddress - should throw exception when session not found")
    void testAddAddress_SessionNotFound() {
        AddressRequest addressRequest = new AddressRequest();
        when(checkoutSessionRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(CheckoutNotFoundException.class, () -> checkoutService.addAddress(999L, 1L, addressRequest));
    }

    @Test
    @DisplayName("Test addAddress - should throw exception for expired session")
    void testAddAddress_ExpiredSession() {
        testSession.setExpiresAt(LocalDateTime.now().minusMinutes(10)); // Expired
        AddressRequest addressRequest = new AddressRequest();
        addressRequest.setShippingAddress("123 Main St");

        when(checkoutSessionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testSession));

        assertThrows(CheckoutExpiredException.class, () -> checkoutService.addAddress(1L, 1L, addressRequest));
    }

    @Test
    @DisplayName("Test getCheckoutSession - should return session")
    void testGetCheckoutSession_Success() {
        when(checkoutSessionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testSession));

        CheckoutResponse response = checkoutService.getCheckoutSession(1L, 1L);

        assertNotNull(response);
        assertEquals(1L, response.getSessionId());
        assertEquals(59.98, response.getTotalAmount());
    }

    @Test
    @DisplayName("Test cancelCheckout - should cancel checkout session")
    void testCancelCheckout_Success() {
        when(checkoutSessionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testSession));

        checkoutService.cancelCheckout(1L, 1L);

        assertEquals(CheckoutStatus.CANCELLED, testSession.getStatus());
        verify(checkoutSessionRepository).save(testSession);
    }

    @Test
    @DisplayName("Test cancelCheckout - should throw exception for completed checkout")
    void testCancelCheckout_AlreadyCompleted() {
        testSession.setStatus(CheckoutStatus.COMPLETED);
        when(checkoutSessionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testSession));

        assertThrows(IllegalArgumentException.class, () -> checkoutService.cancelCheckout(1L, 1L));
    }
}
