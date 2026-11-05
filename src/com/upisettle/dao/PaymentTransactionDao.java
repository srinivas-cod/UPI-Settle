package com.upisettle.dao;

import com.upisettle.model.PaymentMethod;
import com.upisettle.model.PaymentStatus;
import com.upisettle.model.PaymentTransaction;
import com.upisettle.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentTransactionDao {

    public void save(PaymentTransaction transaction) throws SQLException {
        String sql = "INSERT INTO payment_transactions " +
                     "(transaction_id, merchant_id, amount, status, payment_method, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE amount = VALUES(amount), status = VALUES(status)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindTransaction(ps, transaction);
            ps.executeUpdate();
        }
    }

    public void saveAll(List<PaymentTransaction> transactions) throws SQLException {
        String sql = "INSERT INTO payment_transactions " +
                     "(transaction_id, merchant_id, amount, status, payment_method, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE amount = VALUES(amount), status = VALUES(status)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false); // Start transaction block

            for (PaymentTransaction tx : transactions) {
                bindTransaction(ps, tx);
                ps.addBatch();
            }

            ps.executeBatch();
            conn.commit(); // Commit transaction atomically

        } catch (SQLException e) {
            throw new SQLException("Failed batch insert of payment transactions: " + e.getMessage(), e);
        }
    }

    public List<PaymentTransaction> findAll() throws SQLException {
        String sql = "SELECT transaction_id, merchant_id, amount, status, payment_method, created_at " +
                     "FROM payment_transactions ORDER BY transaction_id";

        List<PaymentTransaction> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }

        return list;
    }

    public List<PaymentTransaction> findByStatus(PaymentStatus status) throws SQLException {
        String sql = "SELECT transaction_id, merchant_id, amount, status, payment_method, created_at " +
                     "FROM payment_transactions WHERE status = ? ORDER BY transaction_id";

        List<PaymentTransaction> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }

        return list;
    }

    public void deleteAll() throws SQLException {
        String sql = "DELETE FROM payment_transactions";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    private void bindTransaction(PreparedStatement ps, PaymentTransaction tx) throws SQLException {
        ps.setString(1, tx.getTransactionId());
        ps.setString(2, tx.getMerchantId());
        ps.setBigDecimal(3, tx.getAmount());
        ps.setString(4, tx.getStatus().name());
        ps.setString(5, tx.getPaymentMethod().name());

        LocalDateTime time = tx.getTimestamp() != null ? tx.getTimestamp() : LocalDateTime.now();
        ps.setTimestamp(6, Timestamp.valueOf(time));
    }

    private PaymentTransaction mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime time = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

        return new PaymentTransaction(
                rs.getString("transaction_id"),
                rs.getString("merchant_id"),
                rs.getBigDecimal("amount"),
                PaymentStatus.valueOf(rs.getString("status")),
                PaymentMethod.valueOf(rs.getString("payment_method")),
                time
        );
    }
}
