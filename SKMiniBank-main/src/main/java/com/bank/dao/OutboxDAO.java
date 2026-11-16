package com.bank.dao;

import com.bank.model.OutboxEvent;
import com.bank.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OutboxDAO {

    public static void ensureTableExists() {
        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS integration_outbox (" +
                         "outbox_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "event_id VARCHAR(100) UNIQUE NOT NULL, " +
                         "event_type VARCHAR(50) NOT NULL, " +
                         "aggregate_type VARCHAR(50) NOT NULL, " +
                         "aggregate_id VARCHAR(100) NOT NULL, " +
                         "payload TEXT NOT NULL, " +
                         "status VARCHAR(20) DEFAULT 'PENDING', " +
                         "retry_count INT DEFAULT 0, " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                         "processed_at TIMESTAMP NULL)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean saveEvent(Connection con, OutboxEvent event) throws SQLException {
        String sql = "INSERT INTO integration_outbox (event_id, event_type, aggregate_type, aggregate_id, payload, status) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, event.getEventId());
            ps.setString(2, event.getEventType());
            ps.setString(3, event.getAggregateType());
            ps.setString(4, event.getAggregateId());
            ps.setString(5, event.getPayload());
            ps.setString(6, "PENDING");
            return ps.executeUpdate() > 0;
        }
    }

    public boolean saveEvent(OutboxEvent event) {
        ensureTableExists();
        try (Connection con = DBConnection.getConnection()) {
            return saveEvent(con, event);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<OutboxEvent> getPendingEvents(int limit) {
        ensureTableExists();
        List<OutboxEvent> list = new ArrayList<>();
        String sql = "SELECT * FROM integration_outbox WHERE status = 'PENDING' AND retry_count < 5 ORDER BY created_at ASC LIMIT ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit > 0 ? limit : 20);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OutboxEvent e = new OutboxEvent();
                    e.setOutboxId(rs.getInt("outbox_id"));
                    e.setEventId(rs.getString("event_id"));
                    e.setEventType(rs.getString("event_type"));
                    e.setAggregateType(rs.getString("aggregate_type"));
                    e.setAggregateId(rs.getString("aggregate_id"));
                    e.setPayload(rs.getString("payload"));
                    e.setStatus(rs.getString("status"));
                    e.setRetryCount(rs.getInt("retry_count"));
                    e.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(e);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean markProcessed(String eventId) {
        ensureTableExists();
        String sql = "UPDATE integration_outbox SET status = 'PUBLISHED', processed_at = NOW() WHERE event_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, eventId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
