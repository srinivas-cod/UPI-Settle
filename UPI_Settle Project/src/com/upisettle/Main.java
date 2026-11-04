package com.upisettle;

import com.upisettle.model.PaymentTransaction;
import com.upisettle.model.ReconciliationResult;
import com.upisettle.model.ReconciliationSummary;
import com.upisettle.model.ExceptionReport;
import com.upisettle.model.SettlementRecord;
import com.upisettle.service.ReconciliationService;
import com.upisettle.service.ReconciliationSummaryService;
import com.upisettle.service.ExceptionReportService;
import com.upisettle.simulator.PaymentSimulator;
import com.upisettle.simulator.SettlementSimulator;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        // STEP 1: Generate payment transactions

        PaymentSimulator paymentSimulator = new PaymentSimulator();

        List<PaymentTransaction> transactions =
                paymentSimulator.generateTransactions("MERCHANT001", 20);


        // STEP 2: Display all input transactions

        System.out.println("===== INPUT TRANSACTIONS =====");

        for (PaymentTransaction transaction : transactions) {

            System.out.println(
                    transaction.getTransactionId()
                            + " | " + transaction.getStatus()
            );
        }


        // STEP 3: Generate settlement records

        SettlementSimulator settlementSimulator =
                new SettlementSimulator();

        List<SettlementRecord> settlements =
                settlementSimulator.generateSettlements(transactions);


        // STEP 4: Perform reconciliation

        ReconciliationService reconciliationService =
                new ReconciliationService();

        List<ReconciliationResult> results =
                reconciliationService.reconcile(transactions, settlements);


        // STEP 5: Display reconciliation results

        System.out.println();
        System.out.println("===== RECONCILIATION RESULTS =====");

        for (ReconciliationResult result : results) {

            System.out.println(
                    result.getTransactionId()
                            + " | " + result.getResult()
                            + " | Payment: ₹" + result.getPaymentAmount()
                            + " | Settlement: ₹" + result.getSettlementAmount()
            );
        }


        // STEP 6: Generate reconciliation summary

        ReconciliationSummaryService summaryService =
                new ReconciliationSummaryService();

        ReconciliationSummary summary =
                summaryService.generateSummary(transactions, results);


        // STEP 7: Display reconciliation summary

        System.out.println();
        System.out.println("===== RECONCILIATION SUMMARY =====");

        System.out.println("Total Input Transactions : "
                + summary.getTotalInputTransactions());

        System.out.println("Successful Transactions  : "
                + summary.getSuccessfulTransactions());

        System.out.println("Pending Transactions     : "
                + summary.getPendingTransactions());

        System.out.println("Failed Transactions      : "
                + summary.getFailedTransactions());

        System.out.println("Refunded Transactions    : "
                + summary.getRefundedTransactions());

        System.out.println("----------------------------------");

        System.out.println("Total Reconciled Results : "
                + summary.getTotalResults());

        System.out.println("Matched Transactions     : "
                + summary.getMatchedCount());

        System.out.println("Amount Mismatches        : "
                + summary.getMismatchCount());

        System.out.println("Missing Settlements      : "
                + summary.getMissingCount());

        System.out.println("Duplicate Settlements    : "
                + summary.getDuplicateCount());

        System.out.println("Total Exceptions         : "
                + summary.getExceptionCount());

        System.out.println("Total Matched Amount     : ₹"
                + summary.getTotalMatchedAmount());

        System.out.println("Match Rate               : "
                + summary.getMatchRate() + "%");

        System.out.println("==================================");


        // STEP 8: Generate exception report

        ExceptionReportService exceptionService =
                new ExceptionReportService();

        List<ExceptionReport> exceptions =
                exceptionService.generateExceptionReport(results);


        // STEP 9: Display detailed exception report

        System.out.println();
        System.out.println("===== DETAILED EXCEPTION REPORT =====");

        for (ExceptionReport exception : exceptions) {

            System.out.println();
            System.out.println("Transaction ID    : " + exception.getTransactionId());
            System.out.println("Exception Type    : " + exception.getExceptionType());
            System.out.println("Payment Amount    : ₹" + exception.getPaymentAmount());

            if (exception.getSettlementAmount() != null) {
                System.out.println("Settlement Amount : ₹" + exception.getSettlementAmount());
            } else {
                System.out.println("Settlement Amount : N/A");
            }

            if ("AMOUNT_MISMATCH".equals(exception.getExceptionType()) && exception.getAmountDifference() != null) {
                System.out.println("Amount Difference : ₹" + exception.getAmountDifference());
            } else if ("DUPLICATE_SETTLEMENT".equals(exception.getExceptionType())) {
                System.out.println("Duplicate Records : " + exception.getDuplicateCount());
            }

            System.out.println("Reason            : " + exception.getReason());
            System.out.println();
            System.out.println("-------------------------------------");
        }

        System.out.println();
        System.out.println("Total Exceptions: " + exceptions.size());
    }
}
