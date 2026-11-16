package com.bank.controller;

import com.bank.dao.AdminDAO;
import com.bank.integration.OtpChallenge;
import com.bank.integration.aws.AwsEmailSender;
import com.bank.integration.aws.AwsSmsSender;
import com.bank.model.Admin;
import com.bank.service.AdminAuditService;
import com.bank.util.PasswordUtil;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/AdminLoginServlet")
public class AdminLoginServlet extends HttpServlet {

    private final AdminDAO adminDAO = new AdminDAO();

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String adminIdInput = request.getParameter("adminId");
        String passwordInput = request.getParameter("password");
        String clientIp = AdminAuditService.getClientIp(request);

        if (adminIdInput == null || adminIdInput.trim().isEmpty() || passwordInput == null || passwordInput.trim().isEmpty()) {
            request.setAttribute("error", "Admin ID and Password are required.");
            request.getRequestDispatcher("SKMiniBankadmin-login.jsp").forward(request, response);
            return;
        }

        Admin admin = adminDAO.getAdminById(adminIdInput.trim());

        if (admin == null) {
            AdminAuditService.log("UNKNOWN", "ADMIN_LOGIN_FAILURE", "AUTH", adminIdInput.trim(), clientIp, "FAILURE", "Admin ID not found.");
            request.setAttribute("error", "Invalid Admin ID or Password");
            request.getRequestDispatcher("SKMiniBankadmin-login.jsp").forward(request, response);
            return;
        }

        if (admin.isAccountLocked()) {
            AdminAuditService.log(admin.getAdminId(), "ADMIN_LOGIN_LOCKED", "AUTH", admin.getAdminId(), clientIp, "FAILURE", "Account locked due to consecutive failed attempts.");
            request.setAttribute("error", "Account is locked due to 5 failed login attempts. Please try again after 15 minutes.");
            request.getRequestDispatcher("SKMiniBankadmin-login.jsp").forward(request, response);
            return;
        }

        if (!"ACTIVE".equalsIgnoreCase(admin.getStatus())) {
            AdminAuditService.log(admin.getAdminId(), "ADMIN_LOGIN_INACTIVE", "AUTH", admin.getAdminId(), clientIp, "FAILURE", "Account status is inactive.");
            request.setAttribute("error", "Account is inactive. Contact system administrator.");
            request.getRequestDispatcher("SKMiniBankadmin-login.jsp").forward(request, response);
            return;
        }

        boolean validPassword = PasswordUtil.verifyPassword(passwordInput, admin.getPasswordHash());

        if (!validPassword) {
            adminDAO.incrementFailedAttempts(admin.getAdminId(), 5, 15);
            AdminAuditService.log(admin.getAdminId(), "ADMIN_LOGIN_BAD_PASSWORD", "AUTH", admin.getAdminId(), clientIp, "FAILURE", "Incorrect password entered.");
            request.setAttribute("error", "Invalid Admin ID or Password");
            request.getRequestDispatcher("SKMiniBankadmin-login.jsp").forward(request, response);
            return;
        }

        // Password Verified -> Initiate Dual-OTP MFA Flow
        HttpSession session = request.getSession(true);
        session.setAttribute("admin_auth_pending_id", admin.getAdminId());

        // Auto-generate initial Email & Mobile OTPs for immediate verification readiness
        String email = admin.getEmail();
        String mobile = formatE164(admin.getMobile());

        String emailOtp = OtpChallenge.generate();
        String mobileOtp = OtpChallenge.generate();

        OtpChallenge.store(request, "ADMIN_EMAIL", email, emailOtp);
        OtpChallenge.store(request, "ADMIN_MOBILE", mobile, mobileOtp);

        try {
            AwsEmailSender.send(email, "SK Mini Bank - Admin Login Verification OTP", "Your Admin Login Email OTP is: " + emailOtp + ". Valid for 5 minutes.");
        } catch (Exception ignored) {}

        try {
            AwsSmsSender.send(mobile, "SK Mini Bank Admin Mobile OTP: " + mobileOtp + ". Valid for 5 minutes.");
        } catch (Exception ignored) {}

        AdminAuditService.log(admin.getAdminId(), "PASSWORD_VERIFIED_MFA_STARTED", "AUTH", admin.getAdminId(), clientIp, "SUCCESS", "Password verified. Dual OTPs dispatched.");

        request.setAttribute("mfaStep", true);
        request.setAttribute("pendingAdminId", admin.getAdminId());
        request.setAttribute("maskedEmail", maskEmail(email));
        request.setAttribute("maskedMobile", maskMobile(mobile));

        request.getRequestDispatcher("SKMiniBankadmin-login.jsp").forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/SKMiniBankadmin-login.jsp");
    }

    private String formatE164(String mobile) {
        if (mobile == null || mobile.trim().isEmpty()) return "";
        String clean = mobile.replaceAll("[^0-9+]", "");
        if (clean.startsWith("+") && clean.length() >= 10) return clean;
        if (clean.length() == 10) return "+91" + clean;
        if (clean.length() == 12 && clean.startsWith("91")) return "+" + clean;
        return clean;
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***@***.com";
        String[] parts = email.split("@");
        String name = parts[0];
        if (name.length() <= 2) return name + "***@" + parts[1];
        return name.substring(0, 2) + "***@" + parts[1];
    }

    private String maskMobile(String mobile) {
        if (mobile == null || mobile.length() < 7) return "******";
        return mobile.substring(0, 3) + "*****" + mobile.substring(mobile.length() - 3);
    }
}
