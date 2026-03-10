package com.revshop.auth.service;

import com.revshop.auth.dto.*;
import com.revshop.auth.entity.User;

/**
 * Service interface for authentication operations.
 * Handles user registration, login, password management, and token validation.
 *
 * Owner: Manjula (Login, Registration and Authentication)
 */
public interface AuthService {

    /**
     * Register a new user.
     * @param request registration details
     * @return success message
     */
    String register(RegisterRequest request);

    /**
     * Authenticate user and return JWT token with user details.
     * @param request login credentials
     * @return authentication response with token and user info
     */
    AuthResponse login(LoginRequest request);

    /**
     * Generate a password reset token for the user.
     * @param request forgot password request with email
     * @return success message
     */
    String forgotPassword(ForgotPasswordRequest request);

    /**
     * Reset user password using reset token.
     * @param request reset password details
     * @return success message
     */
    String resetPassword(ResetPasswordRequest request);

    /**
     * Validate JWT token and return user information.
     * Used by API Gateway for request authorization.
     * @param token JWT token to validate
     * @return validation response with user details
     */
    UserValidationResponse validateToken(String token);

    /**
     * Get user by ID (internal endpoint for other microservices).
     * @param userId user ID
     * @return user entity
     */
    User getUserById(Long userId);
}
