package com.revshop.cart.dto;

public class CartItemResponse {

    private Long cartItemId;
    private Long productId;
    private String productName;
    private Double productPrice;
    private Long sellerId;
    private Integer quantity;
    private Double subtotal;

    // Constructors
    public CartItemResponse() {
    }

    public CartItemResponse(Long cartItemId, Long productId, String productName,
                          Double productPrice, Long sellerId, Integer quantity, Double subtotal) {
        this.cartItemId = cartItemId;
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.sellerId = sellerId;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

    // Getters and Setters
    public Long getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(Long cartItemId) {
        this.cartItemId = cartItemId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(Double productPrice) {
        this.productPrice = productPrice;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }
}
