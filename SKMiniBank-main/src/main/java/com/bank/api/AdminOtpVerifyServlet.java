package com.bank.api;

import com.bank.dao.AdminDAO;
import com.bank.integration.OtpChallenge;
import com.bank.model.Admin;
import com.bank.security.AdminRole;
import com.bank.service.AdminAuditService;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/api/admin/otp/verify")
public class AdminOtpVerifyServlet extends HttpServlet {

    private final AdminDAO adminDAO = new AdminDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        String clientIp = AdminAuditService.getClientIp(request);

        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("admin_auth_pending_id") == null) {
                response.setStatus(401);
                out.print("{\"status\":\"failed\",\"message\":\"Authentication session expired. Please log in again.\"}");
                return;
            }

            String adminId = (String) session.getAttribute("admin_auth_pending_id");
            Admin admin = adminDAO.getAdminById(adminId);

            if (admin == null || !"ACTIVE".equalsIgnoreCase(admin.getStatus()) || admin.isAccountLocked()) {
                response.setStatus(403);
                out.print("{\"status\":\"failed\",\"message\":\"Admin account is unavailable or locked.\"}");
                return;
            }

            String emailOtp = request.getParameter("emailOtp");
            String mobileOtp = request.getParameter("mobileOtp");

            if (emailOtp == null || emailOtp.trim().isEmpty() || mobileOtp == null || mobileOtp.trim().isEmpty()) {
                response.setStatus(400);
                out.print("{\"status\":\"failed\",\"message\":\"Both Email OTP and Mobile OTP are required.\"}");
                return;
            }

            String email = admin.getEmail();
            String mobile = formatE164(admin.getMobile());

            boolean emailOk = OtpChallenge.verify(request, "ADMIN_EMAIL", email, emailOtp.trim());
            boolean mobileOk = OtpChallenge.verify(request, "ADMIN_MOBILE", mobile, mobileOtp.trim());

            if (!emailOk || !mobileOk) {
                StringBuilder errorMsg = new StringBuilder("MFA Verification Failed: ");
                if (!emailOk && !mobileOk) {
                    errorMsg.append("Invalid or expired Email & Mobile OTPs.");
                } else if (!emailOk) {
                    errorMsg.append("Invalid or expired Email OTP.");
                } else {
                    errorMsg.append("Invalid or expired Mobile OTP.");
                }

                AdminAuditService.log(adminId, "VERIFY_MFA_OTP", "MFA", adminId, clientIp, "FAILURE", errorMsg.toString());
                response.setStatus(400);
                out.print("{\"status\":\"failed\",\"message\":\"" + errorMsg.toString() + "\"}");
                return;
            }

            // Both OTPs verified successfully -> Complete Login & Promote Session
            session.removeAttribute("admin_auth_pending_id");

            AdminRole role = AdminRole.fromString(admin.getRole());

            // Set secure authenticated admin session attributes
            session.setAttribute("admin", admin.getAdminId());
            session.setAttribute("admin_user", admin.getUsername());
            session.setAttribute("admin_name", admin.getFullName());
            session.setAttribute("admin_role", role.getRoleName());
            session.setAttribute("admin_role_level", role.getLevel());
            session.setAttribute("last_activity_time", System.currentTimeMillis());

            // Reset failed login attempts in DB
            adminDAO.resetFailedAttempts(admin.getAdminId());

            // Clear OTP session states
            OtpChallenge.clear(session, "ADMIN_EMAIL");
            OtpChallenge.clear(session, "ADMIN_MOBILE");

            AdminAuditService.log(admin.getAdminId(), "ADMIN_LOGIN_SUCCESS", "AUTH", admin.getAdminId(), clientIp, "SUCCESS", "Dual-OTP MFA login successful as " + role.getRoleName());

            String redirectUrl = request.getContextPath() + "/DashboardServlet";
            out.print("{\"status\":\"success\",\"message\":\"Dual MFA Verified Successfully!\",\"redirect\":\"" + redirectUrl + "\"}");

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            out.print("{\"status\":\"error\",\"message\":\"Server Error during OTP verification.\"}");
        } finally {
            out.flush();
            out.close();
        }
    }

    private String formatE164(String mobile) {
        if (mobile == null || mobile.trim().isEmpty()) return "";
        String clean = mobile.replaceAll("[^0-9+]", "");
        if (clean.startsWith("+") && clean.length() >= 10) return clean;
        if (clean.length() == 10) return "+91" + clean;
        if (clean.length() == 12 && clean.startsWith("91")) return "+" + clean;
        return clean;
    }
}
