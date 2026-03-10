package com.revshop.checkout.dto;

import com.revshop.checkout.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public class PaymentRequest {

    @NotNull(message = "Checkout session ID is required")
    private Long checkoutSessionId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private CardDetails cardDetails;

    public PaymentRequest() {
    }

    public PaymentRequest(Long checkoutSessionId, PaymentMethod paymentMethod, CardDetails cardDetails) {
        this.checkoutSessionId = checkoutSessionId;
        this.paymentMethod = paymentMethod;
        this.cardDetails = cardDetails;
    }

    public Long getCheckoutSessionId() {
        return checkoutSessionId;
    }

    public void setCheckoutSessionId(Long checkoutSessionId) {
        this.checkoutSessionId = checkoutSessionId;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public CardDetails getCardDetails() {
        return cardDetails;
    }

    public void setCardDetails(CardDetails cardDetails) {
        this.cardDetails = cardDetails;
    }

    public static class CardDetails {
        private String cardNumber;
        private String cardHolderName;
        private String expiryDate;
        private String cvv;

        public CardDetails() {
        }

        public CardDetails(String cardNumber, String cardHolderName, String expiryDate, String cvv) {
            this.cardNumber = cardNumber;
            this.cardHolderName = cardHolderName;
            this.expiryDate = expiryDate;
            this.cvv = cvv;
        }

        public String getCardNumber() {
            return cardNumber;
        }

        public void setCardNumber(String cardNumber) {
            this.cardNumber = cardNumber;
        }

        public String getCardHolderName() {
            return cardHolderName;
        }

        public void setCardHolderName(String cardHolderName) {
            this.cardHolderName = cardHolderName;
        }

        public String getExpiryDate() {
            return expiryDate;
        }

        public void setExpiryDate(String expiryDate) {
            this.expiryDate = expiryDate;
        }

        public String getCvv() {
            return cvv;
        }

        public void setCvv(String cvv) {
            this.cvv = cvv;
        }
    }
}
