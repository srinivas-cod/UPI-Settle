package com.upisettle.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SettlementRecord {

    private final String settlementId;
    private final String transactionId;
    private final BigDecimal settledAmount;
    private final LocalDateTime settlementTime;

    public SettlementRecord(
            String settlementId,
            String transactionId,
            BigDecimal settledAmount,
            LocalDateTime settlementTime) {

        this.settlementId = settlementId;
        this.transactionId = transactionId;
        this.settledAmount = settledAmount;
        this.settlementTime = settlementTime;
    }

    public String getSettlementId() {
        return settlementId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public BigDecimal getSettledAmount() {
        return settledAmount;
    }

    public LocalDateTime getSettlementTime() {
        return settlementTime;
    }
}