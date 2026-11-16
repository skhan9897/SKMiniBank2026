package com.bank.api;

import com.bank.dao.OnboardingDAO;
import com.bank.integration.OtpChallenge;
import com.bank.integration.aws.AwsSmsSender;
import com.bank.model.OnboardingSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/api/registration/mobile/send-otp", "/api/registration/mobile/verify-otp"})
public class RegistrationMobileOtpServlet extends HttpServlet {

    private final OnboardingDAO dao = new OnboardingDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        String path = request.getServletPath();

        try {
            if (path.endsWith("/send-otp")) {
                String mobile = request.getParameter("mobile");
                if (mobile == null || !mobile.replaceAll("[^0-9]", "").matches("[6-9][0-9]{9}")) {
                    response.setStatus(400);
                    out.print("{\"status\":\"failed\",\"message\":\"Invalid Indian mobile number format.\"}");
                    return;
                }

                String cleanMobile = mobile.replaceAll("[^0-9]", "");
                if (dao.isMobileRegistered(cleanMobile)) {
                    response.setStatus(400);
                    out.print("{\"status\":\"failed\",\"message\":\"Mobile number is already registered with an active account.\"}");
                    return;
                }

                if (OtpChallenge.isCooldownActive(request, "REG_MOBILE", 60000L)) {
                    long remaining = OtpChallenge.getRemainingCooldownSeconds(request, "REG_MOBILE", 60000L);
                    response.setStatus(429);
                    out.print("{\"status\":\"failed\",\"message\":\"Please wait " + remaining + " seconds before requesting a new Mobile OTP.\"}");
                    return;
                }

                String otp = OtpChallenge.generate();
                String e164 = "+91" + cleanMobile;
                OtpChallenge.store(request, "REG_MOBILE", cleanMobile, otp);

                try {
                    AwsSmsSender.send(e164, "SK Mini Bank Onboarding OTP: " + otp + ". Valid for 5 minutes. Do not share.");
                } catch (Exception awsEx) {
                    System.err.println("AWS SMS Send Failure: " + awsEx.getMessage());
                }

                out.print("{\"status\":\"success\",\"message\":\"Mobile OTP sent successfully to +91 " + cleanMobile + "\"}");

            } else if (path.endsWith("/verify-otp")) {
                String mobile = request.getParameter("mobile");
                String otp = request.getParameter("otp");

                if (mobile == null || otp == null) {
                    response.setStatus(400);
                    out.print("{\"status\":\"failed\",\"message\":\"Mobile and OTP required.\"}");
                    return;
                }

                String cleanMobile = mobile.replaceAll("[^0-9]", "");
                boolean valid = OtpChallenge.verify(request, "REG_MOBILE", cleanMobile, otp.trim());

                if (!valid) {
                    response.setStatus(400);
                    out.print("{\"status\":\"failed\",\"message\":\"Invalid or expired Mobile OTP.\"}");
                    return;
                }

                // Create or Update Onboarding Session
                String token = (String) request.getSession().getAttribute("onboarding_session_token");
                if (token == null) {
                    token = "SES_" + UUID.randomUUID().toString().replace("-", "");
                    request.getSession().setAttribute("onboarding_session_token", token);
                }

                OnboardingSession session = dao.getSession(token);
                if (session == null) session = new OnboardingSession();
                session.setSessionToken(token);
                session.setMobile(cleanMobile);
                session.setMobileVerified("YES");
                session.setCurrentStep(2);

                dao.saveSession(session);
                OtpChallenge.clear(request.getSession(false), "REG_MOBILE");

                out.print("{\"status\":\"success\",\"sessionToken\":\"" + token + "\",\"message\":\"Mobile verified successfully!\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            out.print("{\"status\":\"error\",\"message\":\"Server Error in Mobile OTP operation.\"}");
        } finally {
            out.flush();
            out.close();
        }
    }
}
