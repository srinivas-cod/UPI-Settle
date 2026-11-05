package com.upisettle;

import com.upisettle.model.*;
import com.upisettle.service.ExceptionReportService;
import com.upisettle.service.ReconciliationService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReconciliationTest {

    public static void main(String[] args) {
        System.out.println("===== RUNNING ADVANCED EXCEPTION TEST SUITE =====\n");

        List<PaymentTransaction> transactions = new ArrayList<>();
        List<SettlementRecord> settlements = new ArrayList<>();

        // Test 1: Correctly matched transaction
        transactions.add(new PaymentTransaction(
                "TXN_MATCH", "M001", new BigDecimal("1000"),
                PaymentStatus.SUCCESS, PaymentMethod.UPI, LocalDateTime.now()));
        settlements.add(new SettlementRecord(
                "SET001", "TXN_MATCH", new BigDecimal("1000"), LocalDateTime.now()));

        // Test 2: Missing settlement
        transactions.add(new PaymentTransaction(
                "TXN_MISSING", "M001", new BigDecimal("2000"),
                PaymentStatus.SUCCESS, PaymentMethod.UPI, LocalDateTime.now()));
        // (no settlement record added)

        // Test 3: Amount mismatch - Settlement is lower
        transactions.add(new PaymentTransaction(
                "TXN_LOWER", "M001", new BigDecimal("3000"),
                PaymentStatus.SUCCESS, PaymentMethod.UPI, LocalDateTime.now()));
        settlements.add(new SettlementRecord(
                "SET002", "TXN_LOWER", new BigDecimal("2950"), LocalDateTime.now()));

        // Test 4: Amount mismatch - Settlement is higher
        transactions.add(new PaymentTransaction(
                "TXN_HIGHER", "M001", new BigDecimal("4000"),
                PaymentStatus.SUCCESS, PaymentMethod.UPI, LocalDateTime.now()));
        settlements.add(new SettlementRecord(
                "SET003", "TXN_HIGHER", new BigDecimal("4050"), LocalDateTime.now()));

        // Test 5: Duplicate settlement - 2 records
        transactions.add(new PaymentTransaction(
                "TXN_DUP_2", "M001", new BigDecimal("5000"),
                PaymentStatus.SUCCESS, PaymentMethod.UPI, LocalDateTime.now()));
        settlements.add(new SettlementRecord(
                "SET004_A", "TXN_DUP_2", new BigDecimal("5000"), LocalDateTime.now()));
        settlements.add(new SettlementRecord(
                "SET004_B", "TXN_DUP_2", new BigDecimal("5000"), LocalDateTime.now()));

        // Test 6: Duplicate settlement - 3 records (more than 2)
        transactions.add(new PaymentTransaction(
                "TXN_DUP_3", "M001", new BigDecimal("6000"),
                PaymentStatus.SUCCESS, PaymentMethod.UPI, LocalDateTime.now()));
        settlements.add(new SettlementRecord(
                "SET005_A", "TXN_DUP_3", new BigDecimal("6000"), LocalDateTime.now()));
        settlements.add(new SettlementRecord(
                "SET005_B", "TXN_DUP_3", new BigDecimal("6000"), LocalDateTime.now()));
        settlements.add(new SettlementRecord(
                "SET005_C", "TXN_DUP_3", new BigDecimal("6000"), LocalDateTime.now()));

        // Test 7: Non-successful transactions (FAILED, PENDING, REFUNDED) - should be skipped
        transactions.add(new PaymentTransaction(
                "TXN_FAILED", "M001", new BigDecimal("7000"),
                PaymentStatus.FAILED, PaymentMethod.UPI, LocalDateTime.now()));
        transactions.add(new PaymentTransaction(
                "TXN_PENDING", "M001", new BigDecimal("8000"),
                PaymentStatus.PENDING, PaymentMethod.UPI, LocalDateTime.now()));
        transactions.add(new PaymentTransaction(
                "TXN_REFUNDED", "M001", new BigDecimal("9000"),
                PaymentStatus.REFUNDED, PaymentMethod.UPI, LocalDateTime.now()));

        // Execute reconciliation
        ReconciliationService reconciliationService = new ReconciliationService();
        List<ReconciliationResult> results = reconciliationService.reconcile(transactions, settlements);

        // Verification asserts
        boolean allPassed = true;

        // Check result count: 6 SUCCESS transactions must be reconciled, 3 non-SUCCESS skipped
        if (results.size() != 6) {
            System.err.println("FAIL: Expected 6 results, got " + results.size());
            allPassed = false;
        } else {
            System.out.println("[PASS] Only SUCCESS transactions were processed (6/6).");
        }

        // Test 1 verification
        ReconciliationResult rMatch = findResult(results, "TXN_MATCH");
        if (rMatch != null && "MATCHED".equals(rMatch.getResult()) && rMatch.getDuplicateCount() == 1) {
            System.out.println("[PASS] Match test verified.");
        } else {
            System.err.println("FAIL: TXN_MATCH failed");
            allPassed = false;
        }

        // Test 2 verification
        ReconciliationResult rMissing = findResult(results, "TXN_MISSING");
        if (rMissing != null && "MISSING_SETTLEMENT".equals(rMissing.getResult())
                && rMissing.getSettlementAmount() == null
                && "No settlement record found for this transaction".equals(rMissing.getReason())
                && rMissing.getDuplicateCount() == 0) {
            System.out.println("[PASS] Missing settlement test verified (null settlement amount handled).");
        } else {
            System.err.println("FAIL: TXN_MISSING failed");
            allPassed = false;
        }

        // Test 3 verification
        ReconciliationResult rLower = findResult(results, "TXN_LOWER");
        if (rLower != null && "AMOUNT_MISMATCH".equals(rLower.getResult())
                && new BigDecimal("50").compareTo(rLower.getAmountDifference()) == 0
                && "Settlement amount is lower than payment amount".equals(rLower.getReason())) {
            System.out.println("[PASS] Lower settlement mismatch test verified (Diff: " + rLower.getAmountDifference() + ").");
        } else {
            System.err.println("FAIL: TXN_LOWER failed");
            allPassed = false;
        }

        // Test 4 verification
        ReconciliationResult rHigher = findResult(results, "TXN_HIGHER");
        if (rHigher != null && "AMOUNT_MISMATCH".equals(rHigher.getResult())
                && new BigDecimal("50").compareTo(rHigher.getAmountDifference()) == 0
                && "Settlement amount is higher than payment amount".equals(rHigher.getReason())) {
            System.out.println("[PASS] Higher settlement mismatch test verified (Diff: " + rHigher.getAmountDifference() + ").");
        } else {
            System.err.println("FAIL: TXN_HIGHER failed");
            allPassed = false;
        }

        // Test 5 verification
        ReconciliationResult rDup2 = findResult(results, "TXN_DUP_2");
        if (rDup2 != null && "DUPLICATE_SETTLEMENT".equals(rDup2.getResult())
                && rDup2.getDuplicateCount() == 2) {
            System.out.println("[PASS] Duplicate settlement with 2 records verified.");
        } else {
            System.err.println("FAIL: TXN_DUP_2 failed");
            allPassed = false;
        }

        // Test 6 verification
        ReconciliationResult rDup3 = findResult(results, "TXN_DUP_3");
        if (rDup3 != null && "DUPLICATE_SETTLEMENT".equals(rDup3.getResult())
                && rDup3.getDuplicateCount() == 3) {
            System.out.println("[PASS] Duplicate settlement with 3 records (> 2) verified.");
        } else {
            System.err.println("FAIL: TXN_DUP_3 failed");
            allPassed = false;
        }

        // Test 7 verification: FAILED / PENDING / REFUNDED not in results
        if (findResult(results, "TXN_FAILED") == null &&
            findResult(results, "TXN_PENDING") == null &&
            findResult(results, "TXN_REFUNDED") == null) {
            System.out.println("[PASS] FAILED, PENDING, and REFUNDED transactions correctly skipped.");
        } else {
            System.err.println("FAIL: Non-successful transaction was not skipped");
            allPassed = false;
        }

        // Exception report generation verification
        ExceptionReportService reportService = new ExceptionReportService();
        List<ExceptionReport> reports = reportService.generateExceptionReport(results);
        if (reports.size() == 5) {
            System.out.println("[PASS] Exception reports generated successfully (5 exceptions out of 6 results, excluding MATCHED).");
        } else {
            System.err.println("FAIL: Expected 5 exception reports, got " + reports.size());
            allPassed = false;
        }

        System.out.println("\n===== TEST SUITE RESULT: " + (allPassed ? "ALL 7 TESTS PASSED SUCCESSFULLY!" : "SOME TESTS FAILED") + " =====");
    }

    private static ReconciliationResult findResult(List<ReconciliationResult> results, String txnId) {
        for (ReconciliationResult r : results) {
            if (r.getTransactionId().equals(txnId)) {
                return r;
            }
        }
        return null;
    }
}
