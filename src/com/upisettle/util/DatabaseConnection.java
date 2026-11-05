package com.upisettle.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String SERVER_URL =
            "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/upisettle_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD =
            System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "root";

    private static String customUrl = null;
    private static String customUser = null;
    private static String customPassword = null;
    private static String lastErrorMessage = null;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("WARN: MySQL JDBC Driver not found on classpath: " + e.getMessage());
        }
    }

    private DatabaseConnection() {
        // Utility class
    }

    public static void configure(String url, String user, String password) {
        customUrl = url;
        customUser = user;
        customPassword = password;
    }

    public static void setPassword(String password) {
        customPassword = password;
    }

    public static Connection getConnection() throws SQLException {
        String url = customUrl != null ? customUrl : DEFAULT_URL;
        String user = customUser != null ? customUser : DEFAULT_USER;
        String pass = customPassword != null ? customPassword : DEFAULT_PASSWORD;
        return DriverManager.getConnection(url, user, pass);
    }

    public static boolean testConnection() {
        lastErrorMessage = null;
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            lastErrorMessage = e.getMessage();
            return false;
        }
    }

    public static boolean autoInitializeDatabase() {
        String user = customUser != null ? customUser : DEFAULT_USER;
        String pass = customPassword != null ? customPassword : DEFAULT_PASSWORD;

        try (Connection conn = DriverManager.getConnection(SERVER_URL, user, pass);
             Statement stmt = conn.createStatement()) {

            // Create database
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS upisettle_db");
            stmt.executeUpdate("USE upisettle_db");

            // Create tables if they do not exist
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS payment_transactions (" +
                    "  transaction_id VARCHAR(50) PRIMARY KEY," +
                    "  merchant_id VARCHAR(50) NOT NULL," +
                    "  amount DECIMAL(12, 2) NOT NULL," +
                    "  status VARCHAR(20) NOT NULL," +
                    "  payment_method VARCHAR(20) NOT NULL," +
                    "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS settlement_records (" +
                    "  settlement_id VARCHAR(50) PRIMARY KEY," +
                    "  transaction_id VARCHAR(50) NOT NULL," +
                    "  settled_amount DECIMAL(12, 2) NOT NULL," +
                    "  settlement_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "  INDEX idx_settlement_txn (transaction_id)" +
                    ")"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS reconciliation_results (" +
                    "  id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "  transaction_id VARCHAR(50) NOT NULL," +
                    "  result VARCHAR(30) NOT NULL," +
                    "  payment_amount DECIMAL(12, 2) NOT NULL," +
                    "  settlement_amount DECIMAL(12, 2) NULL," +
                    "  amount_difference DECIMAL(12, 2) NULL," +
                    "  reason VARCHAR(255) NOT NULL," +
                    "  duplicate_count INT NOT NULL DEFAULT 0," +
                    "  reconciled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "  INDEX idx_recon_txn (transaction_id)" +
                    ")"
            );

            return true;
        } catch (SQLException e) {
            lastErrorMessage = e.getMessage();
            return false;
        }
    }

    public static String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public static String getUrl() {
        return customUrl != null ? customUrl : DEFAULT_URL;
    }

    public static String getUser() {
        return customUser != null ? customUser : DEFAULT_USER;
    }

    public static String getPassword() {
        return customPassword != null ? customPassword : DEFAULT_PASSWORD;
    }
}
