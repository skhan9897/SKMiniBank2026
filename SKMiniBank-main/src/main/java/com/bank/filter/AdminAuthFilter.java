package com.bank.filter;

import com.bank.security.AdminRole;
import com.bank.service.AdminAuditService;
import com.bank.service.AdminAuthorizationService;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebFilter(filterName = "AdminAuthFilter", urlPatterns = {"/admin/*", "/Admin*", "/api/admin/*"})
public class AdminAuthFilter implements Filter {

    private static final long TIMEOUT_MS = 15 * 60 * 1000L; // 15 Minutes Inactivity Timeout

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        String relativePath = path.substring(contextPath.length());

        // Allow public login/logout and MFA API endpoints without filter blockage
        if (relativePath.equalsIgnoreCase("/SKMiniBankadmin-login.jsp")
                || relativePath.equalsIgnoreCase("/admin-login.jsp")
                || relativePath.equalsIgnoreCase("/AdminLoginServlet")
                || relativePath.equalsIgnoreCase("/AdminLogoutServlet")
                || relativePath.startsWith("/api/admin/otp/")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);

        // 1. Check Authentication
        if (session == null || session.getAttribute("admin") == null) {
            handleUnauthenticated(request, response);
            return;
        }

        // 2. Check Session Inactivity Timeout (15 mins)
        Long lastActivity = (Long) session.getAttribute("last_activity_time");
        long now = System.currentTimeMillis();

        if (lastActivity != null && (now - lastActivity) > TIMEOUT_MS) {
            String adminId = (String) session.getAttribute("admin");
            String clientIp = AdminAuditService.getClientIp(request);
            AdminAuditService.log(adminId, "SESSION_TIMEOUT", "AUTH", adminId, clientIp, "EXPIRED", "Session expired due to 15m inactivity.");
            session.invalidate();

            if (isApiRequest(request)) {
                response.setStatus(401);
                response.setContentType("application/json");
                response.getWriter().write("{\"status\":\"failed\",\"message\":\"Session expired due to 15 minutes of inactivity. Please log in again.\"}");
            } else {
                response.sendRedirect(contextPath + "/SKMiniBankadmin-login.jsp?error=Session+expired+due+to+inactivity");
            }
            return;
        }

        // Update last activity timestamp
        session.setAttribute("last_activity_time", now);

        // 3. Check Role-Based Access Control (RBAC)
        AdminRole role = AdminAuthorizationService.getRole(request);

        // Restrict System Settings and Admin User Management to SUPER_ADMIN
        if (relativePath.contains("/admin-management") || relativePath.contains("/system-settings") || relativePath.startsWith("/api/admin/system/")) {
            if (!role.isAtLeast(AdminRole.SUPER_ADMIN)) {
                handleForbidden(request, response, "Super Admin privilege required.");
                return;
            }
        }

        // Restrict Action Servlets / Operations to at least ADMIN (OPERATOR is Read-Only)
        if (relativePath.contains("Approve") || relativePath.contains("Reject") || relativePath.contains("Dispatch") || relativePath.contains("Deliver") || relativePath.contains("Disburse")) {
            if (!role.isAtLeast(AdminRole.ADMIN)) {
                handleForbidden(request, response, "Administrative privilege required. Operator accounts are read-only.");
                return;
            }
        }

        // Add Security Headers
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    private void handleUnauthenticated(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (isApiRequest(request)) {
            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":\"failed\",\"message\":\"Authentication required to access admin resources.\"}");
        } else {
            response.sendRedirect(request.getContextPath() + "/SKMiniBankadmin-login.jsp?error=Please+log+in+first");
        }
    }

    private void handleForbidden(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
        String adminId = (String) request.getSession().getAttribute("admin");
        String clientIp = AdminAuditService.getClientIp(request);
        AdminAuditService.log(adminId, "ACCESS_DENIED_RBAC", "SECURITY", request.getRequestURI(), clientIp, "FORBIDDEN", message);

        if (isApiRequest(request)) {
            response.setStatus(403);
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":\"forbidden\",\"message\":\"" + message + "\"}");
        } else {
            response.sendRedirect(request.getContextPath() + "/DashboardServlet?error=" + java.net.URLEncoder.encode(message, "UTF-8"));
        }
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String accept = request.getHeader("Accept");
        return uri.startsWith(request.getContextPath() + "/api/") || (accept != null && accept.contains("application/json"));
    }
}
