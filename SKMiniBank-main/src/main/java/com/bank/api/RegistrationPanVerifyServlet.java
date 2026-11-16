package com.bank.api;

import com.bank.integration.pan.ConfiguredPanVerificationProvider;
import com.bank.integration.pan.PanVerificationProvider;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/api/registration/pan/verify")
public class RegistrationPanVerifyServlet extends HttpServlet {

    private final ConfiguredPanVerificationProvider provider = new ConfiguredPanVerificationProvider();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String panNumber = request.getParameter("panNumber");
            String fullName = request.getParameter("fullName");

            if (panNumber == null || !panNumber.trim().toUpperCase().matches("[A-Z]{5}[0-9]{4}[A-Z]{1}")) {
                response.setStatus(400);
                out.print("{\"status\":\"failed\",\"message\":\"Invalid PAN Card format (Expected ABCDE1234F).\"}");
                return;
            }

            PanVerificationProvider.PanResult result = provider.verifyPan(panNumber.trim().toUpperCase(), fullName, null);

            out.print("{\"status\":\"" + (result.isSuccess() ? "success" : "pending_config") + "\",\"message\":\"" + result.getMessage() + "\",\"statusBadge\":\"" + result.getStatus() + "\"}");

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            out.print("{\"status\":\"error\",\"message\":\"PAN verification error.\"}");
        } finally {
            out.flush();
            out.close();
        }
    }
}
