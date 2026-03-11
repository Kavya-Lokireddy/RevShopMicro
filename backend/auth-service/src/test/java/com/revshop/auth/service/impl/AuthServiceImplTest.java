package com.revshop.auth.service.impl;

import com.revshop.auth.config.JwtService;
import com.revshop.auth.dto.*;
import com.revshop.auth.entity.Role;
import com.revshop.auth.entity.User;
import com.revshop.auth.exception.DuplicateEmailException;
import com.revshop.auth.exception.InvalidCredentialsException;
import com.revshop.auth.exception.ResourceNotFoundException;
import com.revshop.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private JwtService jwtService;

    private AuthServiceImpl authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Use a real JwtService instance (it has no external dependencies)
        jwtService = new JwtService();
        authService = new AuthServiceImpl(userRepository, passwordEncoder, jwtService);

        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.BUYER);
        testUser.setActive(true);
        testUser.setCreatedAt(LocalDateTime.now());

        // Setup register request
        registerRequest = new RegisterRequest();
        registerRequest.setName("John Doe");
        registerRequest.setEmail("john@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setRole(Role.BUYER);

        // Setup login request
        loginRequest = new LoginRequest();
        loginRequest.setEmail("john@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    @DisplayName("Test register - should register user successfully")
    void testRegister_Success() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        String result = authService.register(registerRequest);

        assertEquals("User registered successfully", result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Test register - should throw exception for duplicate email")
    void testRegister_DuplicateEmail() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Test register - should set business name for seller")
    void testRegister_Seller() {
        registerRequest.setRole(Role.SELLER);
        registerRequest.setBusinessName("My Shop");

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        String result = authService.register(registerRequest);

        assertEquals("User registered successfully", result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Test login - should login successfully and return token")
    void testLogin_Success() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertNotNull(response.getToken()); // Real JWT token generated
        assertEquals(1L, response.getUserId());
        assertEquals("John Doe", response.getName());
        assertEquals("john@example.com", response.getEmail());
        assertEquals(Role.BUYER, response.getRole());
    }

    @Test
    @DisplayName("Test login - should throw exception for invalid email")
    void testLogin_InvalidEmail() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("Test login - should throw exception for wrong password")
    void testLogin_WrongPassword() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("Test login - should throw exception for deactivated account")
    void testLogin_DeactivatedAccount() {
        testUser.setActive(false);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("Test forgotPassword - should generate reset token")
    void testForgotPassword_Success() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("john@example.com");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        String result = authService.forgotPassword(request);

        assertEquals("Password reset token generated successfully. Check your email.", result);
        assertNotNull(testUser.getResetToken());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Test forgotPassword - should throw exception for non-existent email")
    void testForgotPassword_UserNotFound() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("nonexistent@example.com");

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.forgotPassword(request));
    }

    @Test
    @DisplayName("Test resetPassword - should reset password successfully")
    void testResetPassword_Success() {
        testUser.setResetToken("valid-token");

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("john@example.com");
        request.setToken("valid-token");
        request.setNewPassword("newPassword123");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");

        String result = authService.resetPassword(request);

        assertEquals("Password reset successful", result);
        assertNull(testUser.getResetToken());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Test resetPassword - should throw for invalid token")
    void testResetPassword_InvalidToken() {
        testUser.setResetToken("valid-token");

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("john@example.com");
        request.setToken("wrong-token");
        request.setNewPassword("newPassword123");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        assertThrows(InvalidCredentialsException.class, () -> authService.resetPassword(request));
    }

    @Test
    @DisplayName("Test validateToken - should return valid response for valid token")
    void testValidateToken_ValidToken() {
        // Generate a real token using the real JwtService
        String token = jwtService.generateToken("john@example.com", "BUYER", 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserValidationResponse response = authService.validateToken(token);

        assertTrue(response.isValid());
        assertEquals(1L, response.getUserId());
        assertEquals("john@example.com", response.getEmail());
    }

    @Test
    @DisplayName("Test validateToken - should return invalid for bad token")
    void testValidateToken_InvalidToken() {
        UserValidationResponse response = authService.validateToken("invalid-token");

        assertFalse(response.isValid());
    }
}
