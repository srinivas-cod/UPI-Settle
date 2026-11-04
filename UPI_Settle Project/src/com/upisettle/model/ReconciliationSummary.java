
package com.upisettle.model;

import java.math.BigDecimal;

public class ReconciliationSummary {

    // Input transaction statistics
    private final int totalInputTransactions;
    private final int successfulTransactions;
    private final int pendingTransactions;
    private final int failedTransactions;
    private final int refundedTransactions;

    // Reconciliation statistics
    private final int totalResults;
    private final int matchedCount;
    private final int mismatchCount;
    private final int missingCount;
    private final int duplicateCount;
    private final int exceptionCount;

    // Financial statistics
    private final BigDecimal totalMatchedAmount;
    private final BigDecimal matchRate;

    public ReconciliationSummary(
            int totalInputTransactions,
            int successfulTransactions,
            int pendingTransactions,
            int failedTransactions,
            int refundedTransactions,
            int totalResults,
            int matchedCount,
            int mismatchCount,
            int missingCount,
            int duplicateCount,
            int exceptionCount,
            BigDecimal totalMatchedAmount,
            BigDecimal matchRate) {

        this.totalInputTransactions = totalInputTransactions;
        this.successfulTransactions = successfulTransactions;
        this.pendingTransactions = pendingTransactions;
        this.failedTransactions = failedTransactions;
        this.refundedTransactions = refundedTransactions;

        this.totalResults = totalResults;
        this.matchedCount = matchedCount;
        this.mismatchCount = mismatchCount;
        this.missingCount = missingCount;
        this.duplicateCount = duplicateCount;
        this.exceptionCount = exceptionCount;

        this.totalMatchedAmount = totalMatchedAmount;
        this.matchRate = matchRate;
    }

    // Input transaction getters
    public int getTotalInputTransactions() {
        return totalInputTransactions;
    }

    public int getSuccessfulTransactions() {
        return successfulTransactions;
    }

    public int getPendingTransactions() {
        return pendingTransactions;
    }

    public int getFailedTransactions() {
        return failedTransactions;
    }

    public int getRefundedTransactions() {
        return refundedTransactions;
    }

    // Reconciliation getters
    public int getTotalResults() {
        return totalResults;
    }

    public int getMatchedCount() {
        return matchedCount;
    }

    public int getMismatchCount() {
        return mismatchCount;
    }

    public int getMissingCount() {
        return missingCount;
    }

    public int getDuplicateCount() {
        return duplicateCount;
    }

    public int getExceptionCount() {
        return exceptionCount;
    }

    // Financial getters
    public BigDecimal getTotalMatchedAmount() {
        return totalMatchedAmount;
    }

    public BigDecimal getMatchRate() {
        return matchRate;
    }
}
