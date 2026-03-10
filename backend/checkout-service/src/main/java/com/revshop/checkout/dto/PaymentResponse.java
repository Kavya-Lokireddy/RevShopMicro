package com.revshop.checkout.dto;

import com.revshop.checkout.entity.PaymentStatus;

public class PaymentResponse {

    private String transactionId;
    private PaymentStatus status;
    private Long orderId;
    private String message;

    public PaymentResponse() {
    }

    public PaymentResponse(String transactionId, PaymentStatus status, Long orderId, String message) {
        this.transactionId = transactionId;
        this.status = status;
        this.orderId = orderId;
        this.message = message;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
