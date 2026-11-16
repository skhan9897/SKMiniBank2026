package com.bank.controller;

import com.bank.integration.OtpChallenge;
import com.bank.service.AdminAuditService;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/AdminLogoutServlet")
public class AdminLogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session != null) {
            String adminId = (String) session.getAttribute("admin");
            String clientIp = AdminAuditService.getClientIp(request);

            if (adminId != null) {
                AdminAuditService.log(adminId, "ADMIN_LOGOUT", "AUTH", adminId, clientIp, "SUCCESS", "Admin session logged out.");
            }

            OtpChallenge.clear(session, "ADMIN_EMAIL");
            OtpChallenge.clear(session, "ADMIN_MOBILE");
            session.invalidate();
        }

        response.sendRedirect(request.getContextPath() + "/SKMiniBankadmin-login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
