package com.revshop.cart.dto;

import jakarta.validation.constraints.NotNull;

public class FavoriteRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    // Constructors
    public FavoriteRequest() {
    }

    public FavoriteRequest(Long productId) {
        this.productId = productId;
    }

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
}
