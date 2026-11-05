package com.upisettle.dao;

import com.upisettle.model.SettlementRecord;
import com.upisettle.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SettlementRecordDao {

    public void save(SettlementRecord settlement) throws SQLException {
        String sql = "INSERT INTO settlement_records " +
                     "(settlement_id, transaction_id, settled_amount, settlement_time) " +
                     "VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE settled_amount = VALUES(settled_amount)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindSettlement(ps, settlement);
            ps.executeUpdate();
        }
    }

    public void saveAll(List<SettlementRecord> settlements) throws SQLException {
        String sql = "INSERT INTO settlement_records " +
                     "(settlement_id, transaction_id, settled_amount, settlement_time) " +
                     "VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE settled_amount = VALUES(settled_amount)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false); // Start transaction block

            for (SettlementRecord s : settlements) {
                bindSettlement(ps, s);
                ps.addBatch();
            }

            ps.executeBatch();
            conn.commit(); // Atomic commit

        } catch (SQLException e) {
            throw new SQLException("Failed batch insert of settlement records: " + e.getMessage(), e);
        }
    }

    public List<SettlementRecord> findAll() throws SQLException {
        String sql = "SELECT settlement_id, transaction_id, settled_amount, settlement_time " +
                     "FROM settlement_records ORDER BY settlement_time";

        List<SettlementRecord> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }

        return list;
    }

    public List<SettlementRecord> findByTransactionId(String transactionId) throws SQLException {
        String sql = "SELECT settlement_id, transaction_id, settled_amount, settlement_time " +
                     "FROM settlement_records WHERE transaction_id = ?";

        List<SettlementRecord> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, transactionId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }

        return list;
    }

    public void deleteAll() throws SQLException {
        String sql = "DELETE FROM settlement_records";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    private void bindSettlement(PreparedStatement ps, SettlementRecord s) throws SQLException {
        ps.setString(1, s.getSettlementId());
        ps.setString(2, s.getTransactionId());
        ps.setBigDecimal(3, s.getSettledAmount());

        LocalDateTime time = s.getSettlementTime() != null ? s.getSettlementTime() : LocalDateTime.now();
        ps.setTimestamp(4, Timestamp.valueOf(time));
    }

    private SettlementRecord mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("settlement_time");
        LocalDateTime time = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

        return new SettlementRecord(
                rs.getString("settlement_id"),
                rs.getString("transaction_id"),
                rs.getBigDecimal("settled_amount"),
                time
        );
    }
}
