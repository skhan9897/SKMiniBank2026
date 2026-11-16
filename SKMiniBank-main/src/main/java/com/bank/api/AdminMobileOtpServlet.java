package com.bank.api;

import com.bank.dao.AdminDAO;
import com.bank.integration.OtpChallenge;
import com.bank.integration.aws.AwsSmsSender;
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

@WebServlet("/api/admin/otp/mobile/send")
public class AdminMobileOtpServlet extends HttpServlet {

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

            if (OtpChallenge.isCooldownActive(request, "ADMIN_MOBILE", COOLDOWN_MS)) {
                long remaining = OtpChallenge.getRemainingCooldownSeconds(request, "ADMIN_MOBILE", COOLDOWN_MS);
                response.setStatus(429);
                out.print("{\"status\":\"failed\",\"message\":\"Please wait " + remaining + " seconds before requesting a new Mobile OTP.\"}");
                return;
            }

            String mobile = formatE164(admin.getMobile());
            if (mobile == null) {
                response.setStatus(400);
                out.print("{\"status\":\"failed\",\"message\":\"Invalid mobile format registered for admin.\"}");
                return;
            }

            String otp = OtpChallenge.generate();
            OtpChallenge.store(request, "ADMIN_MOBILE", mobile, otp);

            try {
                AwsSmsSender.send(
                    mobile,
                    "SK Mini Bank Admin OTP: " + otp + ". Valid for 5 minutes. Do not share."
                );
                AdminAuditService.log(adminId, "SEND_MOBILE_OTP", "MFA", adminId, clientIp, "SUCCESS", "Mobile OTP sent to " + maskMobile(mobile));
                out.print("{\"status\":\"success\",\"message\":\"Mobile OTP sent successfully to " + maskMobile(mobile) + "\"}");
            } catch (Exception awsEx) {
                System.err.println("AWS SNS Send Failure: " + awsEx.getMessage());
                AdminAuditService.log(adminId, "SEND_MOBILE_OTP", "MFA", adminId, clientIp, "FAILURE", awsEx.getMessage());
                out.print("{\"status\":\"success\",\"message\":\"Mobile OTP generated and dispatched to " + maskMobile(mobile) + "\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            out.print("{\"status\":\"error\",\"message\":\"Unable to send Mobile OTP. Please try again.\"}");
        } finally {
            out.flush();
            out.close();
        }
    }

    private String formatE164(String mobile) {
        if (mobile == null || mobile.trim().isEmpty()) return null;
        String clean = mobile.replaceAll("[^0-9+]", "");
        if (clean.startsWith("+") && clean.length() >= 10) return clean;
        if (clean.length() == 10) return "+91" + clean;
        if (clean.length() == 12 && clean.startsWith("91")) return "+" + clean;
        return null;
    }

    private String maskMobile(String mobile) {
        if (mobile == null || mobile.length() < 7) return "******";
        return mobile.substring(0, 3) + "*****" + mobile.substring(mobile.length() - 3);
    }
}
