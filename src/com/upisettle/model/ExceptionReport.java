package com.upisettle.model;

import java.math.BigDecimal;

public class ExceptionReport {

    private final String transactionId;
    private final String exceptionType;
    private final BigDecimal paymentAmount;
    private final BigDecimal settlementAmount;
    private final BigDecimal amountDifference;
    private final String reason;
    private final int duplicateCount;

    public ExceptionReport(
            String transactionId,
            String exceptionType,
            BigDecimal paymentAmount,
            BigDecimal settlementAmount) {
        this(transactionId, exceptionType, paymentAmount, settlementAmount, null, null, 0);
    }

    public ExceptionReport(
            String transactionId,
            String exceptionType,
            BigDecimal paymentAmount,
            BigDecimal settlementAmount,
            BigDecimal amountDifference,
            String reason,
            int duplicateCount) {

        this.transactionId = transactionId;
        this.exceptionType = exceptionType;
        this.paymentAmount = paymentAmount;
        this.settlementAmount = settlementAmount;
        this.amountDifference = amountDifference;
        this.reason = reason;
        this.duplicateCount = duplicateCount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public BigDecimal getSettlementAmount() {
        return settlementAmount;
    }

    public BigDecimal getAmountDifference() {
        return amountDifference;
    }

    public String getReason() {
        return reason;
    }

    public int getDuplicateCount() {
        return duplicateCount;
    }
}
