package com.bank.service;

import com.bank.security.AdminRole;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class AdminAuthorizationService {

    public static boolean isAuthenticated(HttpServletRequest request) {
        if (request == null) return false;
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("admin") != null;
    }

    public static AdminRole getRole(HttpServletRequest request) {
        if (!isAuthenticated(request)) return AdminRole.OPERATOR;
        HttpSession session = request.getSession(false);
        String roleStr = (String) session.getAttribute("admin_role");
        return AdminRole.fromString(roleStr);
    }

    public static boolean hasPermission(HttpServletRequest request, AdminRole requiredRole) {
        if (!isAuthenticated(request)) return false;
        AdminRole currentRole = getRole(request);
        return currentRole.isAtLeast(requiredRole);
    }

    public static boolean canManageAdmins(HttpServletRequest request) {
        return hasPermission(request, AdminRole.SUPER_ADMIN);
    }

    public static boolean canApproveOperations(HttpServletRequest request) {
        return hasPermission(request, AdminRole.ADMIN);
    }

    public static boolean canViewDashboard(HttpServletRequest request) {
        return hasPermission(request, AdminRole.OPERATOR);
    }
}
