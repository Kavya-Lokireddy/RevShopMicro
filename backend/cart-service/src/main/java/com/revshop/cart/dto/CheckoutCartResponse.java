package com.revshop.cart.dto;

import java.util.List;

public class CheckoutCartResponse {

    private Long id;
    private Long userId;
    private List<CheckoutCartItemResponse> items;
    private Double totalAmount;

    public CheckoutCartResponse() {
    }

    public CheckoutCartResponse(Long id, Long userId, List<CheckoutCartItemResponse> items, Double totalAmount) {
        this.id = id;
        this.userId = userId;
        this.items = items;
        this.totalAmount = totalAmount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<CheckoutCartItemResponse> getItems() {
        return items;
    }

    public void setItems(List<CheckoutCartItemResponse> items) {
        this.items = items;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }
}
