package com.revshop.cart.dto;

import java.util.List;

public class CartResponse {

    private Long cartId;
    private List<CartItemResponse> items;
    private Double totalPrice;
    private Integer totalItems;

    // Constructors
    public CartResponse() {
    }

    public CartResponse(Long cartId, List<CartItemResponse> items, Double totalPrice, Integer totalItems) {
        this.cartId = cartId;
        this.items = items;
        this.totalPrice = totalPrice;
        this.totalItems = totalItems;
    }

    // Getters and Setters
    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public List<CartItemResponse> getItems() {
        return items;
    }

    public void setItems(List<CartItemResponse> items) {
        this.items = items;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }
}
