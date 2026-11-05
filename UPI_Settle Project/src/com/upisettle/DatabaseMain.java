package com.upisettle;

import com.upisettle.dao.PaymentTransactionDao;
import com.upisettle.dao.ReconciliationResultDao;
import com.upisettle.dao.SettlementRecordDao;
import com.upisettle.model.ExceptionReport;
import com.upisettle.model.PaymentTransaction;
import com.upisettle.model.ReconciliationResult;
import com.upisettle.model.ReconciliationSummary;
import com.upisettle.model.SettlementRecord;
import com.upisettle.service.ExceptionReportService;
import com.upisettle.service.ReconciliationService;
import com.upisettle.service.ReconciliationSummaryService;
import com.upisettle.simulator.PaymentSimulator;
import com.upisettle.simulator.SettlementSimulator;
import com.upisettle.util.DatabaseConnection;

import java.sql.SQLException;
import java.util.List;

public class DatabaseMain {

    public static void main(String[] args) {

        System.out.println("==================================================");
        System.out.println("   UPISettle - MySQL & JDBC Reconciliation Engine ");
        System.out.println("==================================================");

        // 1. Test database connectivity & auto-initialize tables
        System.out.println("\n[1/5] Checking MySQL Connection via JDBC...");
        System.out.println("Target Database : upisettle_db");
        System.out.println("Database User   : " + DatabaseConnection.getUser());

        boolean initialized = DatabaseConnection.autoInitializeDatabase();

        if (!initialized && !DatabaseConnection.testConnection()) {
            String error = DatabaseConnection.getLastErrorMessage();

            System.out.println("\n[X] Could not connect to MySQL Server!");
            if (error != null) {
                System.out.println("Error details   : " + error);
            }
            System.out.println("--------------------------------------------------");
            return;
        }

        System.out.println("[✓] Connected to MySQL! Database and tables verified.");

        PaymentTransactionDao paymentDao = new PaymentTransactionDao();
        SettlementRecordDao settlementDao = new SettlementRecordDao();
        ReconciliationResultDao resultDao = new ReconciliationResultDao();

        try {
            // 2. Clear previous run and seed fresh simulation data
            System.out.println("\n[2/5] Refreshing MySQL database with new transactions...");
            paymentDao.deleteAll();
            settlementDao.deleteAll();
            resultDao.deleteAll();

            PaymentSimulator paymentSimulator = new PaymentSimulator();
            List<PaymentTransaction> generatedPayments = paymentSimulator.generateTransactions("MERCHANT001", 20);
            paymentDao.saveAll(generatedPayments);
            System.out.println("[✓] Inserted " + generatedPayments.size() + " fresh payments into MySQL (with realistic timestamps).");

            SettlementSimulator settlementSimulator = new SettlementSimulator();
            List<SettlementRecord> generatedSettlements = settlementSimulator.generateSettlements(generatedPayments);
            settlementDao.saveAll(generatedSettlements);
            System.out.println("[✓] Inserted " + generatedSettlements.size() + " fresh settlements into MySQL (staggered clearing times).");

            // 3. Read back from MySQL tables (proving roundtrip database retrieval)
            System.out.println("\n[3/5] Querying records from MySQL tables for reconciliation...");
            List<PaymentTransaction> transactions = paymentDao.findAll();
            List<SettlementRecord> settlements = settlementDao.findAll();
            System.out.println("[✓] Fetched " + transactions.size() + " transactions from MySQL 'payment_transactions'.");
            System.out.println("[✓] Fetched " + settlements.size() + " settlement records from MySQL 'settlement_records'.");

            // 4. Execute Core Reconciliation Engine
            System.out.println("\n[4/5] Running Reconciliation Engine on database records...");
            ReconciliationService reconciliationService = new ReconciliationService();
            List<ReconciliationResult> results = reconciliationService.reconcile(transactions, settlements);
            System.out.println("[✓] Reconciliation complete. Generated " + results.size() + " results.");

            // Save results to MySQL
            resultDao.saveAll(results);
            System.out.println("[✓] Persisted " + results.size() + " audited reconciliation results into MySQL.");

            // 5. Generate Summary and Detailed Exception Report
            System.out.println("\n[5/5] Generating Summary & Exception Analysis...");
            ReconciliationSummaryService summaryService = new ReconciliationSummaryService();
            ReconciliationSummary summary = summaryService.generateSummary(transactions, results);

            System.out.println();
            System.out.println("===== RECONCILIATION SUMMARY (FROM DATABASE) ===== ");
            System.out.println("Total Input Transactions : " + summary.getTotalInputTransactions());
            System.out.println("Successful Transactions  : " + summary.getSuccessfulTransactions());
            System.out.println("Pending Transactions     : " + summary.getPendingTransactions());
            System.out.println("Failed Transactions      : " + summary.getFailedTransactions());
            System.out.println("Refunded Transactions    : " + summary.getRefundedTransactions());
            System.out.println("----------------------------------");
            System.out.println("Total Reconciled Results : " + summary.getTotalResults());
            System.out.println("Matched Transactions     : " + summary.getMatchedCount());
            System.out.println("Amount Mismatches        : " + summary.getMismatchCount());
            System.out.println("Missing Settlements      : " + summary.getMissingCount());
            System.out.println("Duplicate Settlements    : " + summary.getDuplicateCount());
            System.out.println("Total Exceptions         : " + summary.getExceptionCount());
            System.out.println("Total Matched Amount     : ₹" + summary.getTotalMatchedAmount());
            System.out.println("Match Rate               : " + summary.getMatchRate() + "%");
            System.out.println("==================================");

            ExceptionReportService exceptionService = new ExceptionReportService();
            List<ExceptionReport> exceptions = exceptionService.generateExceptionReport(results);

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
            System.out.println("\n[✓] Fresh run complete! New records successfully saved in MySQL.");

        } catch (SQLException e) {
            System.err.println("[X] Database error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
