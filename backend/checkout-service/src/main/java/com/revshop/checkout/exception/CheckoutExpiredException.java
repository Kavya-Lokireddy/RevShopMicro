package com.revshop.checkout.exception;

public class CheckoutExpiredException extends RuntimeException {
    public CheckoutExpiredException(String message) {
        super(message);
    }
}
