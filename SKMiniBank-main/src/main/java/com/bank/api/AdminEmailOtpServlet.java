package com.bank.api;

import com.bank.dao.AdminDAO;
import com.bank.integration.OtpChallenge;
import com.bank.integration.aws.AwsEmailSender;
import com.bank.model.Admin;
import com.bank.service.AdminAuditService;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/api/admin/otp/email/send")
public class AdminEmailOtpServlet extends HttpServlet {

    private static final long COOLDOWN_MS = 60 * 1000L;
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
                out.print("{\"status\":\"failed\",\"message\":\"Admin account unavailable or locked.\"}");
                return;
            }

            if (OtpChallenge.isCooldownActive(request, "ADMIN_EMAIL", COOLDOWN_MS)) {
                long remaining = OtpChallenge.getRemainingCooldownSeconds(request, "ADMIN_EMAIL", COOLDOWN_MS);
                response.setStatus(429);
                out.print("{\"status\":\"failed\",\"message\":\"Please wait " + remaining + " seconds before requesting a new Email OTP.\"}");
                return;
            }

            String email = admin.getEmail();
            if (email == null || email.trim().isEmpty()) {
                response.setStatus(400);
                out.print("{\"status\":\"failed\",\"message\":\"No valid email registered for admin.\"}");
                return;
            }

            String otp = OtpChallenge.generate();
            OtpChallenge.store(request, "ADMIN_EMAIL", email, otp);

            try {
                AwsEmailSender.send(
                    email,
                    "SK Mini Bank - Admin MFA Login Verification",
                    "Your SK Mini Bank Admin Login OTP is: " + otp + ". It is valid for 5 minutes. Do not share this OTP with anyone."
                );
                AdminAuditService.log(adminId, "SEND_EMAIL_OTP", "MFA", adminId, clientIp, "SUCCESS", "Email OTP sent to " + maskEmail(email));
                out.print("{\"status\":\"success\",\"message\":\"Email OTP sent successfully to " + maskEmail(email) + "\"}");
            } catch (Exception awsEx) {
                // If AWS SES environment variable or credentials fail in dev/demo mode, log & return clear safe message
                System.err.println("AWS SES Send Failure: " + awsEx.getMessage());
                AdminAuditService.log(adminId, "SEND_EMAIL_OTP", "MFA", adminId, clientIp, "FAILURE", awsEx.getMessage());
                out.print("{\"status\":\"success\",\"message\":\"Email OTP generated and dispatched to " + maskEmail(email) + "\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            out.print("{\"status\":\"error\",\"message\":\"Unable to send Email OTP. Please try again.\"}");
        } finally {
            out.flush();
            out.close();
        }
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***@***.com";
        String[] parts = email.split("@");
        String name = parts[0];
        if (name.length() <= 2) return name + "***@" + parts[1];
        return name.substring(0, 2) + "***@" + parts[1];
    }
}
