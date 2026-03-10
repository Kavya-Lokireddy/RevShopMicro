package com.revshop.checkout.dto;

import com.revshop.checkout.entity.CheckoutStatus;
import com.revshop.checkout.entity.PaymentMethod;
import com.revshop.checkout.entity.PaymentStatus;

import java.time.LocalDateTime;

public class CheckoutResponse {

    private Long sessionId;
    private Long userId;
    private String cartSnapshot;
    private String shippingAddress;
    private String billingAddress;
    private String contactName;
    private String phoneNumber;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private Double totalAmount;
    private CheckoutStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean expired;

    public CheckoutResponse() {
    }

    public CheckoutResponse(Long sessionId, Long userId, String cartSnapshot,
                           String shippingAddress, String billingAddress, String contactName,
                           String phoneNumber, PaymentMethod paymentMethod, PaymentStatus paymentStatus,
                           Double totalAmount, CheckoutStatus status, LocalDateTime createdAt,
                           LocalDateTime expiresAt, boolean expired) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.cartSnapshot = cartSnapshot;
        this.shippingAddress = shippingAddress;
        this.billingAddress = billingAddress;
        this.contactName = contactName;
        this.phoneNumber = phoneNumber;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.expired = expired;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCartSnapshot() {
        return cartSnapshot;
    }

    public void setCartSnapshot(String cartSnapshot) {
        this.cartSnapshot = cartSnapshot;
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

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public CheckoutStatus getStatus() {
        return status;
    }

    public void setStatus(CheckoutStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }
}
