package com.bank.dao;

import com.bank.model.AdminAudit;
import com.bank.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminAuditDAO {

    public static void ensureTableExists() {
        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS admin_audit_logs (" +
                         "audit_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "admin_id VARCHAR(50), " +
                         "action VARCHAR(100) NOT NULL, " +
                         "module VARCHAR(50) NOT NULL, " +
                         "target_record_id VARCHAR(100), " +
                         "ip_address VARCHAR(50), " +
                         "result VARCHAR(20) NOT NULL, " +
                         "details TEXT, " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean logAction(AdminAudit audit) {
        ensureTableExists();
        String sql = "INSERT INTO admin_audit_logs (admin_id, action, module, target_record_id, ip_address, result, details) VALUES (?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, audit.getAdminId());
            ps.setString(2, audit.getAction());
            ps.setString(3, audit.getModule());
            ps.setString(4, audit.getTargetRecordId());
            ps.setString(5, audit.getIpAddress());
            ps.setString(6, audit.getResult());
            ps.setString(7, audit.getDetails());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<AdminAudit> getRecentLogs(int limit) {
        ensureTableExists();
        List<AdminAudit> list = new ArrayList<>();
        String sql = "SELECT * FROM admin_audit_logs ORDER BY created_at DESC LIMIT ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit > 0 ? limit : 100);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AdminAudit a = new AdminAudit();
                    a.setAuditId(rs.getInt("audit_id"));
                    a.setAdminId(rs.getString("admin_id"));
                    a.setAction(rs.getString("action"));
                    a.setModule(rs.getString("module"));
                    a.setTargetRecordId(rs.getString("target_record_id"));
                    a.setIpAddress(rs.getString("ip_address"));
                    a.setResult(rs.getString("result"));
                    a.setDetails(rs.getString("details"));
                    a.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(a);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
