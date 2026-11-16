
package com.bank.api;

import com.bank.integration.OtpChallenge;
import com.bank.integration.aws.AwsSmsSender;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/api/otp/mobile")
public class MobileOtpApiServlet extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        try {
            String mobile = request.getParameter("mobile");
            String otp = request.getParameter("otp");
            String action = request.getParameter("action");

            if (mobile == null || mobile.trim().isEmpty()) {
                response.setStatus(400);
                out.print("{\"status\":\"failed\",\"message\":\"Mobile number required\"}");
                return;
            }

            mobile = mobile.trim();

            if ("verify".equalsIgnoreCase(action)) {

                boolean valid = OtpChallenge.verify(
                        request,
                        "mobile",
                        mobile,
                        otp
                );

                if (valid) {
                    out.print("{\"status\":\"success\",\"message\":\"Mobile OTP verified\"}");
                } else {
                    out.print("{\"status\":\"failed\",\"message\":\"Invalid or expired OTP\"}");
                }

                return;
            }

            String generatedOtp = OtpChallenge.generate();

            OtpChallenge.store(
                    request,
                    "mobile",
                    mobile,
                    generatedOtp
            );

            try {
                AwsSmsSender.send(
                        mobile,
                        "Your SK Mini Bank OTP is "
                                + generatedOtp
                                + ". Valid for 5 minutes. Do not share it."
                );
            } catch (Exception e) {
                OtpChallenge.clear(request.getSession(false), "mobile");
                throw e;
            }

            out.print("{\"status\":\"success\",\"message\":\"Mobile OTP sent successfully\"}");

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            out.print("{\"status\":\"error\",\"message\":\"Unable to send mobile OTP\"}");
        } finally {
            out.close();
        }
    }
}
