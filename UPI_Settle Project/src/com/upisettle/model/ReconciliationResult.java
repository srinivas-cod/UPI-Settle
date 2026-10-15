package com.upisettle.model;

import java.math.BigDecimal;

public class ReconciliationResult {

    private final String transactionId;
    private final String result;
    private final BigDecimal paymentAmount;
    private final BigDecimal settlementAmount;

    public ReconciliationResult(
            String transactionId,
            String result,
            BigDecimal paymentAmount,
            BigDecimal settlementAmount) {

        this.transactionId = transactionId;
        this.result = result;
        this.paymentAmount = paymentAmount;
        this.settlementAmount = settlementAmount;
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
}