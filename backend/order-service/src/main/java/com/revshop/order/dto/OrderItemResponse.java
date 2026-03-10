package com.revshop.order.dto;

public class OrderItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private Long sellerId;
    private Integer quantity;
    private Double priceAtPurchase;
    private Double subtotal;

    public OrderItemResponse() {
    }

    public OrderItemResponse(Long id, Long productId, String productName, Long sellerId, Integer quantity, Double priceAtPurchase, Double subtotal) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.sellerId = sellerId;
        this.quantity = quantity;
        this.priceAtPurchase = priceAtPurchase;
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

    public Double getPriceAtPurchase() {
        return priceAtPurchase;
    }

    public void setPriceAtPurchase(Double priceAtPurchase) {
        this.priceAtPurchase = priceAtPurchase;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }
}
