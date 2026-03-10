package com.revshop.cart.dto;

public class CheckoutCartItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private Long sellerId;
    private Integer quantity;
    private Double price;
    private Double subtotal;

    public CheckoutCartItemResponse() {
    }

    public CheckoutCartItemResponse(Long id, Long productId, String productName,
                                    Long sellerId, Integer quantity, Double price, Double subtotal) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.sellerId = sellerId;
        this.quantity = quantity;
        this.price = price;
        this.subtotal = subtotal;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }
}
