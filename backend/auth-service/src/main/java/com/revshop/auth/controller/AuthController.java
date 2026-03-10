package com.revshop.auth.controller;

import com.revshop.auth.dto.*;
import com.revshop.auth.entity.User;
import com.revshop.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations.
 * Handles user registration, login, password reset, and token validation.
 *
 * Owner: Manjula (Login, Registration and Authentication)
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Register a new user.
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        String message = authService.register(request);
        return ResponseEntity.ok(message);
    }

    /**
     * Authenticate user and return JWT token.
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Initiate password reset process.
     * POST /api/auth/forgot-password
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String message = authService.forgotPassword(request);
        return ResponseEntity.ok(message);
    }

    /**
     * Reset user password with token.
     * POST /api/auth/reset-password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        String message = authService.resetPassword(request);
        return ResponseEntity.ok(message);
    }

    /**
     * Validate JWT token (internal endpoint for API Gateway).
     * GET /api/auth/validate
     */
    @GetMapping("/validate")
    public ResponseEntity<UserValidationResponse> validateToken(@RequestHeader("Authorization") String authHeader) {
        // Extract token from "Bearer <token>" format
        String token = authHeader.substring(7);
        UserValidationResponse response = authService.validateToken(token);
        return ResponseEntity.ok(response);
    }

    /**
     * Get user by ID (internal endpoint for other microservices).
     * GET /api/auth/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        User user = authService.getUserById(userId);
        return ResponseEntity.ok(user);
    }
}
