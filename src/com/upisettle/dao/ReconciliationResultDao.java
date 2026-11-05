package com.upisettle.dao;

import com.upisettle.model.ReconciliationResult;
import com.upisettle.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ReconciliationResultDao {

    public void saveAll(List<ReconciliationResult> results) throws SQLException {
        String sql = "INSERT INTO reconciliation_results " +
                     "(transaction_id, result, payment_amount, settlement_amount, amount_difference, reason, duplicate_count) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false); // Enable manual transaction control

            for (ReconciliationResult r : results) {
                ps.setString(1, r.getTransactionId());
                ps.setString(2, r.getResult());
                ps.setBigDecimal(3, r.getPaymentAmount());

                // Safe null handling for MISSING_SETTLEMENT
                if (r.getSettlementAmount() != null) {
                    ps.setBigDecimal(4, r.getSettlementAmount());
                } else {
                    ps.setNull(4, Types.DECIMAL);
                }

                // Safe null handling for amountDifference
                if (r.getAmountDifference() != null) {
                    ps.setBigDecimal(5, r.getAmountDifference());
                } else {
                    ps.setNull(5, Types.DECIMAL);
                }

                ps.setString(6, r.getReason());
                ps.setInt(7, r.getDuplicateCount());

                ps.addBatch();
            }

            ps.executeBatch();
            conn.commit(); // Atomic commit

        } catch (SQLException e) {
            throw new SQLException("Failed batch insert of reconciliation results: " + e.getMessage(), e);
        }
    }

    public List<ReconciliationResult> findAll() throws SQLException {
        String sql = "SELECT transaction_id, result, payment_amount, settlement_amount, amount_difference, reason, duplicate_count " +
                     "FROM reconciliation_results ORDER BY id";

        List<ReconciliationResult> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }

        return list;
    }

    public List<ReconciliationResult> findByResultType(String resultType) throws SQLException {
        String sql = "SELECT transaction_id, result, payment_amount, settlement_amount, amount_difference, reason, duplicate_count " +
                     "FROM reconciliation_results WHERE result = ? ORDER BY id";

        List<ReconciliationResult> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, resultType);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }

        return list;
    }

    public void deleteAll() throws SQLException {
        String sql = "DELETE FROM reconciliation_results";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    private ReconciliationResult mapRow(ResultSet rs) throws SQLException {
        String txnId = rs.getString("transaction_id");
        String result = rs.getString("result");
        BigDecimal paymentAmt = rs.getBigDecimal("payment_amount");
        BigDecimal settlementAmt = rs.getBigDecimal("settlement_amount");
        BigDecimal diff = rs.getBigDecimal("amount_difference");
        String reason = rs.getString("reason");
        int dupCount = rs.getInt("duplicate_count");

        return new ReconciliationResult(
                txnId,
                result,
                paymentAmt,
                settlementAmt,
                diff,
                reason,
                dupCount
        );
    }
}
