package com.revshop.order.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false, length = 255)
    private String productName;

    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double priceAtPurchase;

    @Column(nullable = false)
    private Double subtotal;

    public OrderItem() {
    }

    public OrderItem(Long id, Order order, Long productId, String productName, Long sellerId, Integer quantity, Double priceAtPurchase, Double subtotal) {
        this.id = id;
        this.order = order;
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

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
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

    @PrePersist
    @PreUpdate
    protected void calculateSubtotal() {
        if (quantity != null && priceAtPurchase != null) {
            subtotal = quantity * priceAtPurchase;
        }
    }
}
