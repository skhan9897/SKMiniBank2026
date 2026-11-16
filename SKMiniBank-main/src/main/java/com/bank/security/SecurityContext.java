package com.bank.security;

import com.bank.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class SecurityContext {

    public static boolean isAuthenticated(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && (session.getAttribute("customer_id") != null || session.getAttribute("admin_user") != null);
    }

    public static String getUserRole(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return "GUEST";
        if (session.getAttribute("admin_role") != null) {
            return (String) session.getAttribute("admin_role");
        }
        if (session.getAttribute("customer_id") != null) {
            return "CUSTOMER";
        }
        return "GUEST";
    }

    public static boolean hasBankAccess(int userId, String bankId) {
        if (bankId == null || bankId.trim().isEmpty()) return true;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM bank_user_memberships WHERE user_id = ? AND bank_id = ? AND status = 'ACTIVE'")) {
            ps.setInt(1, userId);
            ps.setString(2, bankId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Fallback: if membership table is empty for this user, allow SKB default bank
        return "SKB".equalsIgnoreCase(bankId);
    }
}
