package com.revshop.auth.dto;

import com.revshop.auth.entity.Role;

/**
 * DTO for user validation response.
 * Used by API Gateway to validate JWT tokens and get user details.
 */
public class UserValidationResponse {

    private Long userId;
    private String email;
    private Role role;
    private boolean valid;

    public UserValidationResponse() {
    }

    public UserValidationResponse(Long userId, String email, Role role, boolean valid) {
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.valid = valid;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
