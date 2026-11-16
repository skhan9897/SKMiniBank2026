package com.bank.api;

import com.bank.integration.aadhaar.ConfiguredAadhaarOtpProvider;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/api/registration/aadhaar/verify")
public class RegistrationAadhaarVerifyServlet extends HttpServlet {

    private final ConfiguredAadhaarOtpProvider provider = new ConfiguredAadhaarOtpProvider();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String aadhaarNumber = request.getParameter("aadhaarNumber");
            if (aadhaarNumber == null || !aadhaarNumber.replaceAll("[^0-9]", "").matches("[0-9]{12}")) {
                response.setStatus(400);
                out.print("{\"status\":\"failed\",\"message\":\"Invalid 12-digit Aadhaar Card number format.\"}");
                return;
            }

            try {
                provider.requestOtp(aadhaarNumber.replaceAll("[^0-9]", ""), "USER_CONSENT");
                out.print("{\"status\":\"success\",\"message\":\"Aadhaar OTP requested successfully.\"}");
            } catch (Exception e) {
                // Fail closed gracefully when provider unconfigured without faking verification
                out.print("{\"status\":\"pending_config\",\"message\":\"Aadhaar Verification Pending Configuration. Required UIDAI provider credentials not set.\",\"statusBadge\":\"PENDING_CONFIG\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            out.print("{\"status\":\"error\",\"message\":\"Aadhaar verification error.\"}");
        } finally {
            out.flush();
            out.close();
        }
    }
}
