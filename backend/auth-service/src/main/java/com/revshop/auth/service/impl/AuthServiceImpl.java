package com.revshop.auth.service.impl;

import com.revshop.auth.config.JwtService;
import com.revshop.auth.dto.*;
import com.revshop.auth.entity.User;
import com.revshop.auth.exception.DuplicateEmailException;
import com.revshop.auth.exception.InvalidCredentialsException;
import com.revshop.auth.exception.ResourceNotFoundException;
import com.revshop.auth.repository.UserRepository;
import com.revshop.auth.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Implementation of AuthService interface.
 * Provides authentication and user management functionality.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public String register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already exists: " + request.getEmail());
        }

        // Create new user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setActive(true);

        // Set business name for sellers
        if (request.getRole().name().equals("SELLER")) {
            user.setBusinessName(request.getBusinessName());
        }

        userRepository.save(user);
        return "User registered successfully";
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Check if user is active
        if (!user.getActive()) {
            throw new InvalidCredentialsException("Account is deactivated");
        }

        // Generate JWT token
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name(), user.getId());

        // Return response with token and user details
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    @Override
    public String forgotPassword(ForgotPasswordRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        userRepository.save(user);

        // In production, send email with reset token
        // For now, return success message
        return "Password reset token generated successfully. Check your email.";
    }

    @Override
    public String resetPassword(ResetPasswordRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        // Validate reset token
        if (user.getResetToken() == null || !user.getResetToken().equals(request.getToken())) {
            throw new InvalidCredentialsException("Invalid or expired reset token");
        }

        // Update password and clear reset token
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        userRepository.save(user);

        return "Password reset successful";
    }

    @Override
    public UserValidationResponse validateToken(String token) {
        try {
            // Validate token and extract claims
            if (jwtService.validateToken(token)) {
                String email = jwtService.extractEmail(token);
                String role = jwtService.extractRole(token);
                Long userId = jwtService.extractUserId(token);

                // Verify user still exists and is active
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                if (!user.getActive()) {
                    return new UserValidationResponse(null, null, null, false);
                }

                return new UserValidationResponse(userId, email, user.getRole(), true);
            }
            return new UserValidationResponse(null, null, null, false);
        } catch (Exception e) {
            return new UserValidationResponse(null, null, null, false);
        }
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
}
