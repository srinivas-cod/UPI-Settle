package com.upisettle;

import com.upisettle.model.PaymentTransaction;
import com.upisettle.model.ReconciliationResult;
import com.upisettle.model.SettlementRecord;
import com.upisettle.service.ReconciliationService;
import com.upisettle.simulator.PaymentSimulator;
import com.upisettle.simulator.SettlementSimulator;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        PaymentSimulator paymentSimulator =
                new PaymentSimulator();

        List<PaymentTransaction> transactions =
                paymentSimulator.generateTransactions(
                        "MERCHANT001",
                        20
                );

        SettlementSimulator settlementSimulator =
                new SettlementSimulator();

        List<SettlementRecord> settlements =
                settlementSimulator.generateSettlements(
                        transactions
                );

        ReconciliationService reconciliationService =
                new ReconciliationService();

        List<ReconciliationResult> results =
                reconciliationService.reconcile(
                        transactions,
                        settlements
                );

        System.out.println("===== RECONCILIATION RESULTS =====");

        for (ReconciliationResult result : results) {

            System.out.println(
                    result.getTransactionId()
                            + " | "
                            + result.getResult()
                            + " | Payment: ₹"
                            + result.getPaymentAmount()
                            + " | Settlement: ₹"
                            + result.getSettlementAmount()
            );
        }
    }
}