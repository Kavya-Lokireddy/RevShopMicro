package com.revshop.checkout.dto;

import java.util.List;

public class CreateOrderRequest {

    private Long userId;
    private String shippingAddress;
    private String billingAddress;
    private String contactName;
    private String phoneNumber;
    private String paymentMethod;
    private String paymentStatus;
    private Double totalAmount;
    private List<OrderItemRequest> items;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(Long userId, String shippingAddress, String billingAddress,
                              String contactName, String phoneNumber, String paymentMethod,
                              String paymentStatus, Double totalAmount, List<OrderItemRequest> items) {
        this.userId = userId;
        this.shippingAddress = shippingAddress;
        this.billingAddress = billingAddress;
        this.contactName = contactName;
        this.phoneNumber = phoneNumber;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.totalAmount = totalAmount;
        this.items = items;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }

    public static class OrderItemRequest {
        private Long productId;
        private String productName;
        private Long sellerId;
        private Integer quantity;
        private Double priceAtPurchase;

        public OrderItemRequest() {
        }

        public OrderItemRequest(Long productId, String productName, Long sellerId, Integer quantity, Double priceAtPurchase) {
            this.productId = productId;
            this.productName = productName;
            this.sellerId = sellerId;
            this.quantity = quantity;
            this.priceAtPurchase = priceAtPurchase;
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
    }
}
