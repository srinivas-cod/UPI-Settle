-- ======================================================
-- UPISettle - Database Schema & Initial Setup
-- Database: MySQL 8.0+ / 9.0+
-- ======================================================

CREATE DATABASE IF NOT EXISTS upisettle_db;
USE upisettle_db;

-- 1. Table for payment transactions received from payment gateways
CREATE TABLE IF NOT EXISTS payment_transactions (
    transaction_id VARCHAR(50) PRIMARY KEY,
    merchant_id VARCHAR(50) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Table for settlement records received from bank / clearing house
CREATE TABLE IF NOT EXISTS settlement_records (
    settlement_id VARCHAR(50) PRIMARY KEY,
    transaction_id VARCHAR(50) NOT NULL,
    settled_amount DECIMAL(12, 2) NOT NULL,
    settlement_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_settlement_txn (transaction_id)
);

-- 3. Table for persisting reconciled results and audit trail
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
    INDEX idx_recon_txn (transaction_id),
    INDEX idx_recon_result (result)
);

-- ======================================================
-- Sample Seed Data (For testing reconciliation via JDBC)
-- ======================================================

-- Clear previous test data
TRUNCATE TABLE payment_transactions;
TRUNCATE TABLE settlement_records;
TRUNCATE TABLE reconciliation_results;

-- 1. Correct Match Sample
INSERT INTO payment_transactions (transaction_id, merchant_id, amount, status, payment_method)
VALUES ('TXN1001', 'MERCHANT001', 1500.00, 'SUCCESS', 'UPI');
INSERT INTO settlement_records (settlement_id, transaction_id, settled_amount)
VALUES ('SET1001', 'TXN1001', 1500.00);

-- 2. Amount Mismatch Sample (Bank under-settled by ₹10)
INSERT INTO payment_transactions (transaction_id, merchant_id, amount, status, payment_method)
VALUES ('TXN1002', 'MERCHANT001', 2500.00, 'SUCCESS', 'QR');
INSERT INTO settlement_records (settlement_id, transaction_id, settled_amount)
VALUES ('SET1002', 'TXN1002', 2490.00);

-- 3. Missing Settlement Sample (Payment succeeded, bank never settled)
INSERT INTO payment_transactions (transaction_id, merchant_id, amount, status, payment_method)
VALUES ('TXN1003', 'MERCHANT001', 3200.00, 'SUCCESS', 'UPI_INTENT');

-- 4. Duplicate Settlement Sample (Bank processed payment twice)
INSERT INTO payment_transactions (transaction_id, merchant_id, amount, status, payment_method)
VALUES ('TXN1004', 'MERCHANT001', 4500.00, 'SUCCESS', 'UPI');
INSERT INTO settlement_records (settlement_id, transaction_id, settled_amount)
VALUES ('SET1004A', 'TXN1004', 4500.00);
INSERT INTO settlement_records (settlement_id, transaction_id, settled_amount)
VALUES ('SET1004B', 'TXN1004', 4500.00);

-- 5. Non-Successful Payment (Should be skipped by reconciliation engine)
INSERT INTO payment_transactions (transaction_id, merchant_id, amount, status, payment_method)
VALUES ('TXN1005', 'MERCHANT001', 5000.00, 'FAILED', 'UPI');
