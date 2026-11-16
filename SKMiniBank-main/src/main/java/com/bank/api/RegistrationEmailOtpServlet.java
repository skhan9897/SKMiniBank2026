package com.bank.api;

import com.bank.dao.OnboardingDAO;
import com.bank.integration.OtpChallenge;
import com.bank.integration.aws.AwsEmailSender;
import com.bank.model.OnboardingSession;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/api/registration/email/send-otp", "/api/registration/email/verify-otp"})
public class RegistrationEmailOtpServlet extends HttpServlet {

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
                String email = request.getParameter("email");
                if (email == null || !email.trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                    response.setStatus(400);
                    out.print("{\"status\":\"failed\",\"message\":\"Invalid Email Address format.\"}");
                    return;
                }

                String cleanEmail = email.trim().toLowerCase();
                if (dao.isEmailRegistered(cleanEmail)) {
                    response.setStatus(400);
                    out.print("{\"status\":\"failed\",\"message\":\"Email address is already registered with an account.\"}");
                    return;
                }

                if (OtpChallenge.isCooldownActive(request, "REG_EMAIL", 60000L)) {
                    long remaining = OtpChallenge.getRemainingCooldownSeconds(request, "REG_EMAIL", 60000L);
                    response.setStatus(429);
                    out.print("{\"status\":\"failed\",\"message\":\"Please wait " + remaining + " seconds before requesting a new Email OTP.\"}");
                    return;
                }

                String otp = OtpChallenge.generate();
                OtpChallenge.store(request, "REG_EMAIL", cleanEmail, otp);

                try {
                    AwsEmailSender.send(cleanEmail, "SK Mini Bank Email Verification OTP", "Your SK Mini Bank Email Verification OTP is " + otp + ". Valid for 5 minutes.");
                } catch (Exception awsEx) {
                    System.err.println("AWS SES Send Failure: " + awsEx.getMessage());
                }

                out.print("{\"status\":\"success\",\"message\":\"Email OTP sent successfully to " + cleanEmail + "\"}");

            } else if (path.endsWith("/verify-otp")) {
                String email = request.getParameter("email");
                String otp = request.getParameter("otp");

                if (email == null || otp == null) {
                    response.setStatus(400);
                    out.print("{\"status\":\"failed\",\"message\":\"Email and OTP required.\"}");
                    return;
                }

                String cleanEmail = email.trim().toLowerCase();
                boolean valid = OtpChallenge.verify(request, "REG_EMAIL", cleanEmail, otp.trim());

                if (!valid) {
                    response.setStatus(400);
                    out.print("{\"status\":\"failed\",\"message\":\"Invalid or expired Email OTP.\"}");
                    return;
                }

                String token = (String) request.getSession().getAttribute("onboarding_session_token");
                OnboardingSession session = dao.getSession(token);
                if (session == null) {
                    response.setStatus(400);
                    out.print("{\"status\":\"failed\",\"message\":\"Mobile verification required before email verification.\"}");
                    return;
                }

                session.setEmail(cleanEmail);
                session.setEmailVerified("YES");
                session.setCurrentStep(3);

                dao.saveSession(session);
                OtpChallenge.clear(request.getSession(false), "REG_EMAIL");

                out.print("{\"status\":\"success\",\"sessionToken\":\"" + token + "\",\"message\":\"Email verified successfully!\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            out.print("{\"status\":\"error\",\"message\":\"Server Error in Email OTP operation.\"}");
        } finally {
            out.flush();
            out.close();
        }
    }
}
