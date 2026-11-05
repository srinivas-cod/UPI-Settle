package com.upisettle.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentTransaction {

    private final String transactionId;
    private final String merchantId;
    private final BigDecimal amount;
    private final PaymentStatus status;
    private final PaymentMethod paymentMethod;
    private final LocalDateTime timestamp;

    public PaymentTransaction(
            String transactionId,
            String merchantId,
            BigDecimal amount,
            PaymentStatus status,
            PaymentMethod paymentMethod,
            LocalDateTime timestamp) {

        this.transactionId = transactionId;
        this.merchantId = merchantId;
        this.amount = amount;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.timestamp = timestamp;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}