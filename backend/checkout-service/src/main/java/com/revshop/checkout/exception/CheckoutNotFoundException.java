package com.revshop.checkout.exception;

public class CheckoutNotFoundException extends RuntimeException {
    public CheckoutNotFoundException(String message) {
        super(message);
    }
}
