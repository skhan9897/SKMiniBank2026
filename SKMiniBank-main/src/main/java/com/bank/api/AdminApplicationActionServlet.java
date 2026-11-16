package com.bank.api;

import com.bank.dao.OnboardingDAO;
import com.bank.integration.aws.AwsEmailSender;
import com.bank.integration.aws.AwsSmsSender;
import com.bank.model.AccountApplication;
import com.bank.service.AdminAuditService;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(urlPatterns = {"/api/admin/applications/approve", "/api/admin/applications/reject"})
public class AdminApplicationActionServlet extends HttpServlet {

    private final OnboardingDAO dao = new OnboardingDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        String path = request.getServletPath();
        String clientIp = AdminAuditService.getClientIp(request);

        try {
            HttpSession session = request.getSession(false);
            String adminId = (session != null && session.getAttribute("admin") != null) ? session.getAttribute("admin").toString() : "ADMIN";

            String appRef = request.getParameter("applicationRef");
            if (appRef == null || appRef.trim().isEmpty()) {
                response.setStatus(400);
                out.print("{\"status\":\"failed\",\"message\":\"Application Reference required.\"}");
                return;
            }

            AccountApplication app = dao.getApplicationByRef(appRef.trim());
            if (app == null) {
                response.setStatus(404);
                out.print("{\"status\":\"failed\",\"message\":\"Application reference not found.\"}");
                return;
            }

            if (path.endsWith("/approve")) {
                boolean ok = dao.approveApplicationAndCreateAccount(appRef.trim(), adminId);
                if (ok) {
                    AdminAuditService.log(adminId, "APPROVE_KYC_ACCOUNT", "KYC", appRef.trim(), clientIp, "SUCCESS", "KYC approved and digital account activated with 0 initial balance.");

                    try {
                        AwsEmailSender.send(app.getEmail(), "SK Mini Bank - Account Activated!",
                            "Dear " + app.getFullName() + ",\n\nCongratulations! Your SK Mini Bank Account Opening Application (" + appRef + ") has been APPROVED by Bank Admin.\n\nYour digital bank account is now ACTIVE with ₹0.00 initial balance.");
                    } catch (Exception ignored) {}

                    try {
                        AwsSmsSender.send("+91" + app.getMobile(), "SK Mini Bank: Congratulations! Your Account Application " + appRef + " has been APPROVED & ACTIVATED.");
                    } catch (Exception ignored) {}

                    out.print("{\"status\":\"success\",\"message\":\"Account Application " + appRef + " approved and activated successfully!\"}");
                } else {
                    response.setStatus(400);
                    out.print("{\"status\":\"failed\",\"message\":\"Unable to approve application. It may have already been processed.\"}");
                }

            } else if (path.endsWith("/reject")) {
                String reason = request.getParameter("reason");
                if (reason == null || reason.trim().isEmpty()) reason = "KYC Document criteria not met.";

                boolean ok = dao.rejectApplication(appRef.trim(), adminId, reason.trim());
                if (ok) {
                    AdminAuditService.log(adminId, "REJECT_KYC_ACCOUNT", "KYC", appRef.trim(), clientIp, "REJECTED", "Application rejected: " + reason);

                    try {
                        AwsEmailSender.send(app.getEmail(), "SK Mini Bank - Application Status Update (" + appRef + ")",
                            "Dear " + app.getFullName() + ",\n\nYour SK Mini Bank Account Opening Application (" + appRef + ") has been REJECTED.\nReason: " + reason);
                    } catch (Exception ignored) {}

                    try {
                        AwsSmsSender.send("+91" + app.getMobile(), "SK Mini Bank: Your Application " + appRef + " was rejected. Reason: " + reason);
                    } catch (Exception ignored) {}

                    out.print("{\"status\":\"success\",\"message\":\"Application " + appRef + " rejected.\"}");
                } else {
                    response.setStatus(400);
                    out.print("{\"status\":\"failed\",\"message\":\"Unable to reject application.\"}");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            out.print("{\"status\":\"error\",\"message\":\"Server Error during application processing.\"}");
        } finally {
            out.flush();
            out.close();
        }
    }
}
