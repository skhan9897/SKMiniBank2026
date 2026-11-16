package com.bank.dao;

import com.bank.model.Admin;
import com.bank.util.DBConnection;
import com.bank.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminDAO {

    public static void ensureTableExists() {
        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS admin (" +
                         "admin_id VARCHAR(50) PRIMARY KEY, " +
                         "username VARCHAR(100) NOT NULL UNIQUE, " +
                         "password_hash VARCHAR(255) NOT NULL, " +
                         "full_name VARCHAR(100) NOT NULL, " +
                         "email VARCHAR(100) NOT NULL UNIQUE, " +
                         "mobile VARCHAR(20) NOT NULL UNIQUE, " +
                         "role VARCHAR(30) NOT NULL DEFAULT 'ADMIN', " +
                         "status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', " +
                         "failed_login_attempts INT DEFAULT 0, " +
                         "locked_until TIMESTAMP NULL, " +
                         "email_verified VARCHAR(10) DEFAULT 'YES', " +
                         "mobile_verified VARCHAR(10) DEFAULT 'YES', " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                         "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                         "last_login_at TIMESTAMP NULL)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Admin getAdminById(String adminId) {
        ensureTableExists();
        if (adminId == null || adminId.trim().isEmpty()) return null;
        String sql = "SELECT * FROM admin WHERE admin_id = ? OR username = ? OR email = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, adminId.trim());
            ps.setString(2, adminId.trim());
            ps.setString(3, adminId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addAdmin(Admin admin) {
        ensureTableExists();
        String sql = "INSERT INTO admin (admin_id, username, password_hash, full_name, email, mobile, role, status) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, admin.getAdminId());
            ps.setString(2, admin.getUsername());
            ps.setString(3, admin.getPasswordHash());
            ps.setString(4, admin.getFullName());
            ps.setString(5, admin.getEmail());
            ps.setString(6, admin.getMobile());
            ps.setString(7, admin.getRole());
            ps.setString(8, admin.getStatus());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePassword(String adminId, String newPassword) {
        ensureTableExists();
        String hash = PasswordUtil.hashPassword(newPassword);
        String sql = "UPDATE admin SET password_hash = ? WHERE admin_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setString(2, adminId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean incrementFailedAttempts(String adminId, int maxAttempts, int lockDurationMinutes) {
        ensureTableExists();
        Admin admin = getAdminById(adminId);
        if (admin == null) return false;

        int attempts = admin.getFailedLoginAttempts() + 1;
        Timestamp lockTime = null;
        if (attempts >= maxAttempts) {
            lockTime = new Timestamp(System.currentTimeMillis() + (lockDurationMinutes * 60 * 1000L));
        }

        String sql = "UPDATE admin SET failed_login_attempts = ?, locked_until = ? WHERE admin_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, attempts);
            ps.setTimestamp(2, lockTime);
            ps.setString(3, admin.getAdminId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean resetFailedAttempts(String adminId) {
        ensureTableExists();
        String sql = "UPDATE admin SET failed_login_attempts = 0, locked_until = NULL, last_login_at = NOW() WHERE admin_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, adminId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Admin> getAllAdmins() {
        ensureTableExists();
        List<Admin> list = new ArrayList<>();
        String sql = "SELECT * FROM admin ORDER BY created_at DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Admin mapRow(ResultSet rs) throws SQLException {
        Admin a = new Admin();
        a.setAdminId(rs.getString("admin_id"));
        a.setUsername(rs.getString("username"));
        a.setPasswordHash(rs.getString("password_hash"));
        a.setFullName(rs.getString("full_name"));
        a.setEmail(rs.getString("email"));
        a.setMobile(rs.getString("mobile"));
        a.setRole(rs.getString("role"));
        a.setStatus(rs.getString("status"));
        a.setFailedLoginAttempts(rs.getInt("failed_login_attempts"));
        a.setLockedUntil(rs.getTimestamp("locked_until"));
        a.setEmailVerified(rs.getString("email_verified"));
        a.setMobileVerified(rs.getString("mobile_verified"));
        a.setCreatedAt(rs.getTimestamp("created_at"));
        a.setUpdatedAt(rs.getTimestamp("updated_at"));
        a.setLastLoginAt(rs.getTimestamp("last_login_at"));
        return a;
    }
}
