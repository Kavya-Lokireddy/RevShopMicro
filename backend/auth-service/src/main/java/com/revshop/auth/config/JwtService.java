package com.revshop.auth.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT service for generating and validating tokens.
 * Handles token creation with 24-hour expiration and claim extraction.
 */
@Service
public class JwtService {

    // Secret key for signing JWT tokens (in production, use environment variable)
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(
            "revshop-secret-key-for-jwt-token-generation-p3-2024".getBytes()
    );

    // Token expiration time: 24 hours in milliseconds
    private static final long EXPIRATION_TIME = 86400000L;

    /**
     * Generate JWT token with user details.
     * @param email user's email
     * @param role user's role
     * @param userId user's ID
     * @return JWT token string
     */
    public String generateToken(String email, String role, Long userId) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract email from JWT token.
     * @param token JWT token
     * @return email from token subject
     */
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Extract role from JWT token.
     * @param token JWT token
     * @return role claim value
     */
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    /**
     * Extract user ID from JWT token.
     * @param token JWT token
     * @return userId claim value
     */
    public Long extractUserId(String token) {
        return extractClaims(token).get("userId", Long.class);
    }

    /**
     * Validate JWT token.
     * Checks signature and expiration.
     * @param token JWT token to validate
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extract all claims from JWT token.
     * @param token JWT token
     * @return Claims object
     */
    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
