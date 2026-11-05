package com.upisettle.service;

import com.upisettle.model.PaymentStatus;
import com.upisettle.model.PaymentTransaction;
import com.upisettle.model.ReconciliationResult;
import com.upisettle.model.SettlementRecord;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ReconciliationService {

    public List<ReconciliationResult> reconcile(
            List<PaymentTransaction> transactions,
            List<SettlementRecord> settlements) {

        List<ReconciliationResult> results = new ArrayList<>();

        for (PaymentTransaction transaction : transactions) {

            if (transaction.getStatus() != PaymentStatus.SUCCESS) {
                continue;
            }

            List<SettlementRecord> matchingSettlements = new ArrayList<>();

            for (SettlementRecord settlement : settlements) {
                if (settlement.getTransactionId().equals(transaction.getTransactionId())) {
                    matchingSettlements.add(settlement);
                }
            }

            if (matchingSettlements.isEmpty()) {

                results.add(
                        new ReconciliationResult(
                                transaction.getTransactionId(),
                                "MISSING_SETTLEMENT",
                                transaction.getAmount(),
                                null,
                                null,
                                "No settlement record found for this transaction",
                                0
                        )
                );

            } else if (matchingSettlements.size() > 1) {

                int duplicateCount = matchingSettlements.size();
                BigDecimal settledAmount = matchingSettlements.get(0).getSettledAmount();

                results.add(
                        new ReconciliationResult(
                                transaction.getTransactionId(),
                                "DUPLICATE_SETTLEMENT",
                                transaction.getAmount(),
                                settledAmount,
                                null,
                                "Multiple settlement records found for this transaction",
                                duplicateCount
                        )
                );

            } else {

                SettlementRecord settlement = matchingSettlements.get(0);
                BigDecimal paymentAmount = transaction.getAmount();
                BigDecimal settledAmount = settlement.getSettledAmount();

                if (paymentAmount.compareTo(settledAmount) == 0) {

                    results.add(
                            new ReconciliationResult(
                                    transaction.getTransactionId(),
                                    "MATCHED",
                                    paymentAmount,
                                    settledAmount,
                                    BigDecimal.ZERO,
                                    "Payment and settlement amounts match successfully",
                                    1
                            )
                    );

                } else {

                    BigDecimal difference = paymentAmount.subtract(settledAmount).abs();
                    String reason;

                    if (settledAmount.compareTo(paymentAmount) < 0) {
                        reason = "Settlement amount is lower than payment amount";
                    } else {
                        reason = "Settlement amount is higher than payment amount";
                    }

                    results.add(
                            new ReconciliationResult(
                                    transaction.getTransactionId(),
                                    "AMOUNT_MISMATCH",
                                    paymentAmount,
                                    settledAmount,
                                    difference,
                                    reason,
                                    1
                            )
                    );
                }
            }
        }

        return results;
    }
}
