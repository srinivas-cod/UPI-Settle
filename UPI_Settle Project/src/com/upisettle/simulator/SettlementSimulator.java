package com.upisettle.simulator;

import com.upisettle.model.PaymentStatus;
import com.upisettle.model.PaymentTransaction;
import com.upisettle.model.SettlementRecord;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SettlementSimulator {

    public List<SettlementRecord> generateSettlements(
            List<PaymentTransaction> transactions) {

        List<SettlementRecord> settlements =
                new ArrayList<>();

        int settlementCounter = 1;
        int scenarioCounter = 0;

        for (PaymentTransaction transaction : transactions) {

            // Only successful payments should normally be settled
            if (transaction.getStatus() != PaymentStatus.SUCCESS) {
                continue;
            }

            int scenario = scenarioCounter % 4;

            // Scenario 0: Correct settlement
            if (scenario == 0) {

                settlements.add(
                        new SettlementRecord(
                                String.format("SET%04d", settlementCounter++),
                                transaction.getTransactionId(),
                                transaction.getAmount(),
                                LocalDateTime.now()
                        )
                );
            }

            // Scenario 1: Amount mismatch
            else if (scenario == 1) {

                BigDecimal incorrectAmount =
                        transaction.getAmount().subtract(
                                BigDecimal.TEN
                        );

                settlements.add(
                        new SettlementRecord(
                                String.format("SET%04d", settlementCounter++),
                                transaction.getTransactionId(),
                                incorrectAmount,
                                LocalDateTime.now()
                        )
                );
            }

            // Scenario 2: Missing settlement
            else if (scenario == 2) {

                // Intentionally do nothing
            }

            // Scenario 3: Duplicate settlement
            else {

                settlements.add(
                        new SettlementRecord(
                                String.format("SET%04d", settlementCounter++),
                                transaction.getTransactionId(),
                                transaction.getAmount(),
                                LocalDateTime.now()
                        )
                );

                settlements.add(
                        new SettlementRecord(
                                String.format("SET%04d", settlementCounter++),
                                transaction.getTransactionId(),
                                transaction.getAmount(),
                                LocalDateTime.now()
                        )
                );
            }

            scenarioCounter++;
        }

        return settlements;
    }
}