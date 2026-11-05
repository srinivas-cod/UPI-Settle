
package com.upisettle.service;

import com.upisettle.model.PaymentStatus;
import com.upisettle.model.PaymentTransaction;
import com.upisettle.model.ReconciliationResult;
import com.upisettle.model.ReconciliationSummary;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class ReconciliationSummaryService {

    public ReconciliationSummary generateSummary(
            List<PaymentTransaction> transactions,
            List<ReconciliationResult> results) {

        // STEP 1: Count input transaction statuses

        int successful = 0;
        int pending = 0;
        int failed = 0;
        int refunded = 0;

        for (PaymentTransaction transaction : transactions) {

            if (transaction.getStatus() == PaymentStatus.SUCCESS) {
                successful++;

            } else if (transaction.getStatus() == PaymentStatus.PENDING) {
                pending++;

            } else if (transaction.getStatus() == PaymentStatus.FAILED) {
                failed++;

            } else if (transaction.getStatus() == PaymentStatus.REFUNDED) {
                refunded++;
            }
        }

        // STEP 2: Count reconciliation outcomes

        int matched = 0;
        int mismatch = 0;
        int missing = 0;
        int duplicate = 0;

        BigDecimal totalMatchedAmount = BigDecimal.ZERO;

        for (ReconciliationResult result : results) {

            switch (result.getResult()) {

                case "MATCHED":
                    matched++;
                    totalMatchedAmount =
                            totalMatchedAmount.add(result.getPaymentAmount());
                    break;

                case "AMOUNT_MISMATCH":
                    mismatch++;
                    break;

                case "MISSING_SETTLEMENT":
                    missing++;
                    break;

                case "DUPLICATE_SETTLEMENT":
                    duplicate++;
                    break;
            }
        }

        // STEP 3: Calculate totals

        int totalInputTransactions = transactions.size();
        int totalResults = results.size();

        int exceptionCount = mismatch + missing + duplicate;

        // STEP 4: Calculate match rate

        BigDecimal matchRate = BigDecimal.ZERO;

        if (totalResults > 0) {

            matchRate = BigDecimal.valueOf(matched)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(
                            BigDecimal.valueOf(totalResults),
                            2,
                            RoundingMode.HALF_UP
                    );
        }

        // STEP 5: Create and return summary object

        return new ReconciliationSummary(
                totalInputTransactions,
                successful,
                pending,
                failed,
                refunded,
                totalResults,
                matched,
                mismatch,
                missing,
                duplicate,
                exceptionCount,
                totalMatchedAmount,
                matchRate
        );
    }
}
