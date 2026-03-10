package com.revshop.checkout.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CartDto {

    @JsonProperty("cartId")
    private Long id;
    
    private Long userId;
    private List<CartItemDto> items;
    
    @JsonProperty("totalPrice")
    private Double totalAmount;

    public CartDto() {
    }

    public CartDto(Long id, Long userId, List<CartItemDto> items, Double totalAmount) {
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

    public List<CartItemDto> getItems() {
        return items;
    }

    public void setItems(List<CartItemDto> items) {
        this.items = items;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CartItemDto {
        
        @JsonProperty("cartItemId")
        private Long id;
        
        private Long productId;
        private String productName;
        private Long sellerId;
        private Integer quantity;
        
        @JsonProperty("productPrice")
        private Double price;
        
        private Double subtotal;

        public CartItemDto() {
        }

        public CartItemDto(Long id, Long productId, String productName,
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
}
