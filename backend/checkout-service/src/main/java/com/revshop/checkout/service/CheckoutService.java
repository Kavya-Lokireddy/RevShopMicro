package com.revshop.checkout.service;

import com.revshop.checkout.dto.AddressRequest;
import com.revshop.checkout.dto.CheckoutResponse;
import com.revshop.checkout.dto.InitiateCheckoutRequest;

public interface CheckoutService {

    CheckoutResponse initiateCheckout(InitiateCheckoutRequest request);

    CheckoutResponse addAddress(Long sessionId, Long userId, AddressRequest request);

    CheckoutResponse getCheckoutSession(Long sessionId, Long userId);

    void cancelCheckout(Long sessionId, Long userId);
}
