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

        List<SettlementRecord> settlements = new ArrayList<>();

        int settlementCounter = 1;
        int scenarioCounter = 0;

        for (PaymentTransaction transaction : transactions) {

            // Only successful payments should normally be settled
            if (transaction.getStatus() != PaymentStatus.SUCCESS) {
                continue;
            }

            // Settlement realistically occurs 1-2 hours after the payment
            LocalDateTime settlementTime = transaction.getTimestamp().plusHours(1).plusMinutes(15);

            int scenario = scenarioCounter % 4;

            // Scenario 0: Correct settlement
            if (scenario == 0) {
                settlements.add(
                        new SettlementRecord(
                                String.format("SET%04d", settlementCounter++),
                                transaction.getTransactionId(),
                                transaction.getAmount(),
                                settlementTime
                        )
                );
            }
            // Scenario 1: Amount mismatch
            else if (scenario == 1) {
                BigDecimal incorrectAmount = transaction.getAmount().subtract(BigDecimal.TEN);

                settlements.add(
                        new SettlementRecord(
                                String.format("SET%04d", settlementCounter++),
                                transaction.getTransactionId(),
                                incorrectAmount,
                                settlementTime
                        )
                );
            }
            // Scenario 2: Missing settlement
            else if (scenario == 2) {
                // Intentionally do nothing (bank missed this settlement)
            }
            // Scenario 3: Duplicate settlement
            else {
                settlements.add(
                        new SettlementRecord(
                                String.format("SET%04d", settlementCounter++),
                                transaction.getTransactionId(),
                                transaction.getAmount(),
                                settlementTime
                        )
                );

                settlements.add(
                        new SettlementRecord(
                                String.format("SET%04d", settlementCounter++),
                                transaction.getTransactionId(),
                                transaction.getAmount(),
                                settlementTime.plusMinutes(45) // Duplicate processed 45 mins later
                        )
                );
            }

            scenarioCounter++;
        }

        return settlements;
    }
}
