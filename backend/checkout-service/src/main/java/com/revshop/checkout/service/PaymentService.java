package com.revshop.checkout.service;

import com.revshop.checkout.dto.PaymentRequest;
import com.revshop.checkout.dto.PaymentResponse;
import com.revshop.checkout.entity.PaymentTransaction;

public interface PaymentService {

    PaymentResponse processPayment(PaymentRequest request);

    PaymentTransaction getTransactionById(Long id);

    PaymentTransaction getTransactionByTransactionId(String transactionId);
}
