package com.upisettle.model;

import java.math.BigDecimal;

public class ReconciliationResult {

    private final String transactionId;
    private final String result;
    private final BigDecimal paymentAmount;
    private final BigDecimal settlementAmount;
    private final BigDecimal amountDifference;
    private final String reason;
    private final int duplicateCount;

    public ReconciliationResult(
            String transactionId,
            String result,
            BigDecimal paymentAmount,
            BigDecimal settlementAmount) {
        this(transactionId, result, paymentAmount, settlementAmount, null, null, 0);
    }

    public ReconciliationResult(
            String transactionId,
            String result,
            BigDecimal paymentAmount,
            BigDecimal settlementAmount,
            BigDecimal amountDifference,
            String reason,
            int duplicateCount) {

        this.transactionId = transactionId;
        this.result = result;
        this.paymentAmount = paymentAmount;
        this.settlementAmount = settlementAmount;
        this.amountDifference = amountDifference;
        this.reason = reason;
        this.duplicateCount = duplicateCount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getResult() {
        return result;
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
