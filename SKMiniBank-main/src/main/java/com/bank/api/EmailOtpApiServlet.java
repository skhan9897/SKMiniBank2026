
package com.bank.api;

import com.bank.integration.OtpChallenge;
import com.bank.integration.aws.AwsEmailSender;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/api/otp/email")
public class EmailOtpApiServlet extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        try {
            String email = request.getParameter("email");
            String otp = request.getParameter("otp");
            String action = request.getParameter("action");

            if (email == null || email.trim().isEmpty()) {
                response.setStatus(400);
                out.print("{\"status\":\"failed\",\"message\":\"Email required\"}");
                return;
            }

            email = email.trim().toLowerCase();

            if (!email.matches(
                    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                response.setStatus(400);
                out.print("{\"status\":\"failed\",\"message\":\"Invalid email address\"}");
                return;
            }

            if ("verify".equalsIgnoreCase(action)) {

                boolean valid = OtpChallenge.verify(
                        request,
                        "email",
                        email,
                        otp
                );

                if (valid) {
                    out.print("{\"status\":\"success\",\"message\":\"Email OTP verified\"}");
                } else {
                    out.print("{\"status\":\"failed\",\"message\":\"Invalid or expired OTP\"}");
                }

                return;
            }

            String generatedOtp = OtpChallenge.generate();

            OtpChallenge.store(
                    request,
                    "email",
                    email,
                    generatedOtp
            );

            try {
                AwsEmailSender.send(
                        email,
                        "SK Mini Bank - OTP Verification",
                        "Your SK Mini Bank OTP is "
                                + generatedOtp
                                + ". Valid for 5 minutes. Do not share it."
                );
            } catch (Exception e) {
                OtpChallenge.clear(request.getSession(false), "email");
                throw e;
            }

            out.print("{\"status\":\"success\",\"message\":\"Email OTP sent successfully\"}");

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            out.print("{\"status\":\"error\",\"message\":\"Unable to send email OTP\"}");
        } finally {
            out.close();
        }
    }
}
