package com.revshop.product.dto;

import jakarta.validation.constraints.NotNull;

public class StockUpdateRequest {

    @NotNull(message = "Quantity is required")
    private Integer quantity;

    public StockUpdateRequest() {
    }

    public StockUpdateRequest(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
