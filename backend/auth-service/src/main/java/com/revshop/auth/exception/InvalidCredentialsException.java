package com.revshop.auth.exception;

/**
 * Exception thrown when user provides invalid credentials during authentication.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
