# UPISettle – Merchant Payment Reconciliation Engine

[![Java](https://img.shields.io/badge/Java-23-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![MySQL](https://img.shields.io/badge/MySQL-9.1-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![JDBC](https://img.shields.io/badge/JDBC-API-007396?style=for-the-badge&logo=java&logoColor=white)](https://docs.oracle.com/javase/8/docs/technotes/guides/jdbc/)
[![Architecture](https://img.shields.io/badge/Architecture-DAO%20Pattern-22C55E?style=for-the-badge)](https://en.wikipedia.org/wiki/Data_access_object)
[![Status](https://img.shields.io/badge/Status-Completed-success?style=for-the-badge)]()

> A simulated merchant payment reconciliation engine built using **Core Java**, **Object-Oriented Design**, and **MySQL/JDBC**.  
> UPISettle automates the matching of inbound payment gateway transactions against bank settlement files, computes exact financial variances using `BigDecimal`, diagnoses multi-record duplicate settlements, and records an auditable trail directly into a relational database.

---

## 📌 Problem Statement

In merchant payment workflows (e.g., UPI, QR codes, Payment Gateways), a merchant receives:
1. **Payment Transactions:** Real-time customer transaction records from the gateway switch (e.g., Razorpay, Cashfree, PhonePe).
2. **Settlement Records:** Batch clearing records deposited into the merchant's nodal bank account by the acquiring bank/clearing house.

### The Business Challenge:
* Has every successful customer payment actually been settled by the bank?
* Does the bank settlement amount match the original transaction amount down to the exact paisa/cent?
* Did network retries or batch re-runs cause duplicate bank settlements?
* Which transactions are missing settlements entirely and need operational triage?

**UPISettle automates this reconciliation lifecycle.**

---

## 🏗️ Architecture & Processing Pipeline

```
 ┌────────────────────────────────────────────────────────────────────────┐
 │                      Payment Gateway / Switch Data                     │
 │              (PaymentSimulator / MySQL payment_transactions)           │
 └───────────────────────────────────┬────────────────────────────────────┘
                                     │
                                     ▼
 ┌────────────────────────────────────────────────────────────────────────┐
 │                      Acquiring Bank Settlement Data                    │
 │               (SettlementSimulator / MySQL settlement_records)         │
 └───────────────────────────────────┬────────────────────────────────────┘
                                     │
                                     ▼
 ┌────────────────────────────────────────────────────────────────────────┐
 │                 Core Engine: ReconciliationService                     │
 │   - Filters non-SUCCESS transactions                                   │
 │   - Matches by transactionId                                           │
 │   - Calculates variance with BigDecimal.subtract().abs()               │
 │   - Dynamically counts duplicate clearing records                      │
 └─────────────────┬─────────────────────────────────┬────────────────────┘
                   │                                 │
                   ▼                                 ▼
 ┌──────────────────────────────────┐ ┌──────────────────────────────────┐
 │    ReconciliationSummaryService  │ │      ExceptionReportService      │
 │  - Match Rate (%)                │ │  - Isolates non-MATCHED cases    │
 │  - Volume by Gateway Status      │ │  - Directional Reasoning         │
 │  - Total Matched Monetary Volume │ │  - Safe Null Value Handling      │
 └─────────────────┬────────────────┘ └──────────────┬───────────────────┘
                   │                                 │
                   └────────────────┬────────────────┘
                                    │
                                    ▼
 ┌────────────────────────────────────────────────────────────────────────┐
 │                  Enterprise Persistence: DAO Layer                     │
 │          (PaymentTransactionDao | SettlementRecordDao)                 │
 │                      ReconciliationResultDao                           │
 └──────────────────────────────────┬─────────────────────────────────────┘
                                    │ PreparedStatement Batch Inserts (ACID)
                                    ▼
 ┌────────────────────────────────────────────────────────────────────────┐
 │                    Relational Database: MySQL 9.1                      │
 │                     `upisettle_db` Audit Trail                         │
 └────────────────────────────────────────────────────────────────────────┘
```

---

## ✨ Core Features & Technical Highlights

### 1. Exact Monetary Precision with `BigDecimal`
* Zero floating-point drift. Avoids IEEE 754 precision issues inherent in `double` and `float`.
* Uses `BigDecimal.compareTo() == 0` instead of `.equals()` to safely compare currency across differing decimal scales (e.g., `100.0` vs `100.00`).
* Calculates absolute variance: `paymentAmount.subtract(settledAmount).abs()`.

### 2. Multi-Scenario Reconciliation Engine
Classifies every transaction into one of four deterministic states:
* **`MATCHED`:** Exactly 1 settlement record with matching amounts.
* **`AMOUNT_MISMATCH`:** Exactly 1 settlement record, but amounts differ (diagnoses whether bank underpaid or overpaid).
* **`MISSING_SETTLEMENT`:** Payment succeeded, but no bank settlement was ever deposited.
* **`DUPLICATE_SETTLEMENT`:** Multiple bank clearing records detected for a single transaction (tracks dynamic duplicate count).

### 3. Enterprise DAO Pattern & JDBC Layer
* **Data Access Object (DAO) Pattern:** Decouples SQL statements from business logic.
* **PreparedStatement & SQL Injection Immunity:** All queries use parameterized placeholders (`?`).
* **Batch Execution & ACID Transactions:** Uses `ps.addBatch()` and `ps.executeBatch()` inside `conn.setAutoCommit(false)` $\rightarrow$ `conn.commit()` blocks.
* **Null Safety:** Handles missing settlement amounts with `ps.setNull(index, Types.DECIMAL)`.
* **Zero Resource Leaks:** Uses Java 7+ `try-with-resources` (`AutoCloseable`) on all connections, statements, and result sets.

### 4. Realistic Banking Ledger Simulation
* Timestamps are staggered realistically throughout the business day rather than sharing identical milliseconds.
* Settlements are logged 1–2 hours after payments to reflect actual bank clearing delays.
* Duplicate settlements arrive 45 minutes later to emulate gateway webhook retries.

---

## 📂 Project Structure

```
UPI-Settle/
├── lib/
│   └── mysql-connector-j-9.1.0.jar      # Official MySQL 9.1 JDBC Driver
├── src/
│   └── com/
│       └── upisettle/
│           ├── Main.java                # In-memory standalone engine
│           ├── DatabaseMain.java        # Full database-driven MySQL pipeline
│           ├── ReconciliationTest.java  # Automated unit test suite (7 edge cases)
│           │
│           ├── model/                   # Immutable Domain Models (private final)
│           │   ├── PaymentMethod.java   # Enum: UPI, QR, UPI_INTENT
│           │   ├── PaymentStatus.java   # Enum: SUCCESS, FAILED, PENDING, REFUNDED
│           │   ├── PaymentTransaction.java
│           │   ├── SettlementRecord.java
│           │   ├── ReconciliationResult.java
│           │   ├── ReconciliationSummary.java
│           │   └── ExceptionReport.java # DTO for operational reporting
│           │
│           ├── service/                 # Core Business Logic (100% DB-agnostic)
│           │   ├── ReconciliationService.java
│           │   ├── ReconciliationSummaryService.java
│           │   └── ExceptionReportService.java
│           │
│           ├── dao/                     # Data Access Object Layer (JDBC)
│           │   ├── PaymentTransactionDao.java
│           │   ├── SettlementRecordDao.java
│           │   └── ReconciliationResultDao.java
│           │
│           ├── util/                    # Infrastructure Utilities
│           │   └── DatabaseConnection.java  # Auto-provisioning connection manager
│           │
│           └── simulator/               # Test Data Generation
│               ├── PaymentSimulator.java
│               └── SettlementSimulator.java
│
├── schema.sql                           # MySQL Table DDL & Seed Scripts
├── .gitignore                           # Standard git ignore definitions
└── README.md                            # Complete Project Documentation
```

---

## 🗄️ Database Schema (`schema.sql`)

```sql
CREATE DATABASE IF NOT EXISTS upisettle_db;
USE upisettle_db;

-- Inbound gateway payments
CREATE TABLE IF NOT EXISTS payment_transactions (
    transaction_id VARCHAR(50) PRIMARY KEY,
    merchant_id VARCHAR(50) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bank settlement records
CREATE TABLE IF NOT EXISTS settlement_records (
    settlement_id VARCHAR(50) PRIMARY KEY,
    transaction_id VARCHAR(50) NOT NULL,
    settled_amount DECIMAL(12, 2) NOT NULL,
    settlement_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_settlement_txn (transaction_id)
);

-- Reconciliation audit trail
CREATE TABLE IF NOT EXISTS reconciliation_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(50) NOT NULL,
    result VARCHAR(30) NOT NULL,
    payment_amount DECIMAL(12, 2) NOT NULL,
    settlement_amount DECIMAL(12, 2) NULL,
    amount_difference DECIMAL(12, 2) NULL,
    reason VARCHAR(255) NOT NULL,
    duplicate_count INT NOT NULL DEFAULT 0,
    reconciled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_recon_txn (transaction_id)
);
```

---

## 🚀 Getting Started

### Prerequisites
* **Java Development Kit (JDK):** Version 17+ (JDK 23 recommended).
* **MySQL Server:** Version 8.0+ or 9.0+ (running on port `3306`).
* **IntelliJ IDEA** (or any Java IDE).

### Running in IntelliJ IDEA
1. Open the project folder in IntelliJ IDEA.
2. Ensure MySQL Server is running (`net start MySQL91`).
3. Open [`src/com/upisettle/DatabaseMain.java`](src/com/upisettle/DatabaseMain.java).
4. Click the green **Run ▶** icon.  
   *(The program will auto-create the database, seed test records, reconcile, and persist results).*

### Running via Terminal / Command Line

#### 1. Compile All Files:
```bash
javac -cp "lib/mysql-connector-j-9.1.0.jar;out" -d out src/com/upisettle/*.java src/com/upisettle/*/*.java
```

#### 2. Run Database Reconciliation Pipeline:
```bash
java -cp "out;lib/mysql-connector-j-9.1.0.jar" com.upisettle.DatabaseMain
```

#### 3. Run Automated Edge-Case Test Suite:
```bash
java -cp out com.upisettle.ReconciliationTest
```

---

## 📊 Sample Output

### Reconciliation Summary
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
```

### Detailed Exception Report
```
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

## 💡 Technical Interview Highlights (ECE $\to$ Software Transition)

* **Why `BigDecimal` instead of `double`?**  
  Floating-point binary representations (IEEE 754) lead to precision drift (e.g., `0.1 + 0.2 != 0.3`). In financial reconciliation, regulatory compliance requires exact scale and rounding mode control (`RoundingMode.HALF_UP`).
* **Why `compareTo()` instead of `equals()`?**  
  `BigDecimal.equals()` compares scale as well as value (e.g., `10.0` vs `10.00` is `false`). `compareTo()` compares mathematical value (`10.0.compareTo(10.00) == 0`), making it appropriate for monetary checks.
* **Algorithmic Optimization ($O(P \times S) \to O(P + S)$):**  
  While simulated nested loops operate in $O(P \times S)$, high-volume enterprise engines group settlements by `transactionId` in a `Map<String, List<SettlementRecord>>` ($O(S)$ construction, $O(1)$ lookup), scaling linearly to $O(P + S)$.
* **Separation of Concerns (SRP):**  
  Domain engine output (`ReconciliationResult`) is isolated from operational reporting DTOs (`ExceptionReport`), ensuring changes to customer reports don't impact database audit schemas.

---

## 👤 Author
**Srinivas S**  
* Electronics & Communication Engineering graduate transitioning to Backend & Software Engineering.  
* GitHub: [@srinivas-cod](https://github.com/srinivas-cod)
