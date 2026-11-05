# UPISettle

A merchant payment reconciliation engine built in Core Java and MySQL (JDBC).

In digital payments (like UPI or card gateways), a merchant receives transaction records from a payment gateway and settlement records from a bank. In an ideal case, every payment has one matching bank settlement for the exact same amount. In reality:
- Banks sometimes under-settle or over-settle transactions.
- Network timeouts or retries cause duplicate settlements for the same transaction.
- Some payments succeed on the gateway side but never get deposited by the bank.

I built this project to simulate that workflow, automate the matching process, compute financial discrepancies, and store the audit results in MySQL.

---

## How It Works

1. **Transaction Ingestion:** Payment transactions and bank settlement records are loaded from MySQL tables (`payment_transactions` and `settlement_records`).
2. **Reconciliation Engine:**
   - Filters out non-successful payments (FAILED, PENDING, REFUNDED).
   - Matches transactions against settlements by `transactionId`.
   - Compares amounts using `BigDecimal.compareTo()`.
   - Identifies duplicates dynamically by counting matching records.
3. **Classification:**
   - `MATCHED`: Exactly 1 settlement record with identical amounts.
   - `AMOUNT_MISMATCH`: Exactly 1 settlement record, but amounts differ (identifies whether the settlement was lower or higher).
   - `MISSING_SETTLEMENT`: Payment succeeded, but no bank record exists.
   - `DUPLICATE_SETTLEMENT`: Multiple settlement records exist for the same payment.
4. **Persistence & Reporting:** Results are saved to the `reconciliation_results` table in MySQL and printed as a summary and detailed exception report.

---

## Architecture & Design Decisions

### Why BigDecimal instead of double/float?
Java's `double` and `float` use IEEE 754 binary floating-point representation, which cannot represent numbers like `0.1` precisely. In financial code, even a fraction of a paisa discrepancy violates audit requirements. All monetary fields use `BigDecimal` with `BigDecimal.subtract().abs()` for variances and `compareTo() == 0` for comparisons.

### Why raw JDBC instead of Spring Boot or Hibernate?
I intentionally built this using standard `java.sql` (JDBC) to understand how database connectivity works under the hood—managing connections, `PreparedStatement` parameters, result mapping, and manual transactions (`setAutoCommit(false)` / `commit()`)—before abstracting it behind frameworks.

### Separation of Concerns (DAO Pattern)
The core reconciliation logic (`ReconciliationService`) knows nothing about databases or SQL. It takes plain Java lists and returns results. All SQL statements, connection handling, and row mapping are isolated inside DAO classes (`PaymentTransactionDao`, `SettlementRecordDao`, `ReconciliationResultDao`).

---

## Project Structure

```
├── lib/
│   └── mysql-connector-j-9.1.0.jar     # MySQL JDBC Driver
├── src/com/upisettle/
│   ├── Main.java                       # In-memory runner
│   ├── DatabaseMain.java               # MySQL/JDBC runner
│   ├── ReconciliationTest.java         # Test suite covering 7 edge cases
│   ├── model/                          # Data models (PaymentTransaction, SettlementRecord, etc.)
│   ├── service/                        # Business logic (ReconciliationService, SummaryService)
│   ├── dao/                            # Database access objects (JDBC CRUD)
│   ├── util/                           # DatabaseConnection manager
│   └── simulator/                      # Simulators with realistic timestamps
├── schema.sql                          # MySQL table definitions and seed data
└── README.md
```

---

## Database Schema

```sql
CREATE DATABASE IF NOT EXISTS upisettle_db;
USE upisettle_db;

CREATE TABLE payment_transactions (
    transaction_id VARCHAR(50) PRIMARY KEY,
    merchant_id VARCHAR(50) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE settlement_records (
    settlement_id VARCHAR(50) PRIMARY KEY,
    transaction_id VARCHAR(50) NOT NULL,
    settled_amount DECIMAL(12, 2) NOT NULL,
    settlement_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_settlement_txn (transaction_id)
);

CREATE TABLE reconciliation_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(50) NOT NULL,
    result VARCHAR(30) NOT NULL,
    payment_amount DECIMAL(12, 2) NOT NULL,
    settlement_amount DECIMAL(12, 2) NULL,
    amount_difference DECIMAL(12, 2) NULL,
    reason VARCHAR(255) NOT NULL,
    duplicate_count INT NOT NULL DEFAULT 0,
    reconciled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## How to Run

### Requirements
- Java 17 or higher
- MySQL Server 8.0+ / 9.0+ running on port 3306

### 1. In IntelliJ IDEA
1. Open the project folder in IntelliJ.
2. Make sure MySQL is running (`net start MySQL91` in administrator command prompt).
3. Run `src/com/upisettle/DatabaseMain.java`.  
   *(The app checks if the database exists and creates the tables automatically on first run).*

### 2. From Terminal
```bash
# Compile
javac -cp "lib/mysql-connector-j-9.1.0.jar;out" -d out src/com/upisettle/*.java src/com/upisettle/*/*.java

# Run database reconciliation pipeline
java -cp "out;lib/mysql-connector-j-9.1.0.jar" com.upisettle.DatabaseMain

# Run edge-case tests
java -cp out com.upisettle.ReconciliationTest
```

---

## Sample Output

```
===== RECONCILIATION SUMMARY (FROM DATABASE) ===== 
Total Input Transactions : 20
Successful Transactions  : 13
Pending Transactions     : 3
Failed Transactions      : 3
Refunded Transactions    : 1
----------------------------------
Total Reconciled Results : 13
Matched Transactions     : 4
Amount Mismatches        : 3
Missing Settlements      : 3
Duplicate Settlements    : 3
Total Exceptions         : 9
Total Matched Amount     : ₹7056.00
Match Rate               : 30.77%
==================================

===== DETAILED EXCEPTION REPORT =====

Transaction ID    : TXN0002
Exception Type    : AMOUNT_MISMATCH
Payment Amount    : ₹4386.00
Settlement Amount : ₹4376.00
Amount Difference : ₹10.00
Reason            : Settlement amount is lower than payment amount

-------------------------------------

Transaction ID    : TXN0005
Exception Type    : MISSING_SETTLEMENT
Payment Amount    : ₹226.00
Settlement Amount : N/A
Reason            : No settlement record found for this transaction

-------------------------------------

Transaction ID    : TXN0006
Exception Type    : DUPLICATE_SETTLEMENT
Payment Amount    : ₹4310.00
Settlement Amount : ₹4310.00
Duplicate Records : 2
Reason            : Multiple settlement records found for this transaction

-------------------------------------
Total Exceptions: 9
```

---

## What's Next
- **Phase 4:** Build REST APIs using Spring Boot to expose reconciliation results and exception reports to a frontend.
- **Optimization:** Refactor the matching algorithm from nested loops $O(P \times S)$ to a HashMap lookup $O(P + S)$ for handling large transaction batches.
