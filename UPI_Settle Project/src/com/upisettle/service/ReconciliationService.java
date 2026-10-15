package com.upisettle.service;

import com.upisettle.model.PaymentStatus;
import com.upisettle.model.PaymentTransaction;
import com.upisettle.model.ReconciliationResult;
import com.upisettle.model.SettlementRecord;

import java.util.ArrayList;
import java.util.List;

public class ReconciliationService {

    public List<ReconciliationResult> reconcile(
            List<PaymentTransaction> transactions,
            List<SettlementRecord> settlements) {

        List<ReconciliationResult> results =
                new ArrayList<>();

        for (PaymentTransaction transaction : transactions) {

            if (transaction.getStatus() != PaymentStatus.SUCCESS) {
                continue;
            }

            List<SettlementRecord> matchingSettlements =
                    new ArrayList<>();

            for (SettlementRecord settlement : settlements) {

                if (settlement.getTransactionId()
                        .equals(transaction.getTransactionId())) {

                    matchingSettlements.add(settlement);
                }
            }

            if (matchingSettlements.isEmpty()) {

                results.add(
                        new ReconciliationResult(
                                transaction.getTransactionId(),
                                "MISSING_SETTLEMENT",
                                transaction.getAmount(),
                                null
                        )
                );

            } else if (matchingSettlements.size() > 1) {

                results.add(
                        new ReconciliationResult(
                                transaction.getTransactionId(),
                                "DUPLICATE_SETTLEMENT",
                                transaction.getAmount(),
                                matchingSettlements.get(0)
                                        .getSettledAmount()
                        )
                );

            } else {

                SettlementRecord settlement =
                        matchingSettlements.get(0);

                if (transaction.getAmount()
                        .compareTo(
                                settlement.getSettledAmount()
                        ) == 0) {

                    results.add(
                            new ReconciliationResult(
                                    transaction.getTransactionId(),
                                    "MATCHED",
                                    transaction.getAmount(),
                                    settlement.getSettledAmount()
                            )
                    );

                } else {

                    results.add(
                            new ReconciliationResult(
                                    transaction.getTransactionId(),
                                    "AMOUNT_MISMATCH",
                                    transaction.getAmount(),
                                    settlement.getSettledAmount()
                            )
                    );
                }
            }
        }

        return results;
    }
}