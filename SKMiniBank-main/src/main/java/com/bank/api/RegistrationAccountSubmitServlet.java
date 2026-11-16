package com.bank.api;

import com.bank.dao.OnboardingDAO;
import com.bank.integration.aws.AwsEmailSender;
import com.bank.integration.aws.AwsSmsSender;
import com.bank.model.AccountApplication;
import com.bank.model.OnboardingSession;
import com.bank.model.OutboxEvent;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/api/registration/submit", "/api/registration/status"})
public class RegistrationAccountSubmitServlet extends HttpServlet {

    private final OnboardingDAO dao = new OnboardingDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String token = (String) request.getSession().getAttribute("onboarding_session_token");
            OnboardingSession session = dao.getSession(token);

            if (session == null || !"YES".equals(session.getMobileVerified()) || !"YES".equals(session.getEmailVerified())) {
                response.setStatus(401);
                out.print("{\"status\":\"failed\",\"message\":\"Mobile and Email verification required.\"}");
                return;
            }

            String appRef = "SKM-APP-" + (long)(Math.random() * 90000000L + 10000000L);

            AccountApplication app = new AccountApplication();
            app.setApplicationRef(appRef);
            app.setSessionToken(token);
            app.setFullName(request.getParameter("fullName"));
            app.setFatherName(request.getParameter("fatherName"));
            app.setMotherName(request.getParameter("motherName"));
            app.setDob(request.getParameter("dob"));
            app.setGender(request.getParameter("gender"));
            app.setMaritalStatus(request.getParameter("maritalStatus"));
            app.setOccupation(request.getParameter("occupation"));
            app.setMobile(session.getMobile());
            app.setEmail(session.getEmail());
            app.setAadhaarNumber(request.getParameter("aadhaar"));
            app.setAadhaarStatus("PENDING_CONFIG");
            app.setPanNumber(request.getParameter("pan"));
            app.setPanStatus("PENDING_CONFIG");
            app.setAddress(request.getParameter("address"));
            app.setCity(request.getParameter("city"));
            app.setState(request.getParameter("state"));
            app.setPincode(request.getParameter("pincode"));
            app.setAccountType(request.getParameter("accountType") != null ? request.getParameter("accountType") : "Savings Account");
            app.setBranch(request.getParameter("branch") != null ? request.getParameter("branch") : "Main Digital Branch");
            app.setInitialBalance(0.0); // Zero Initial Balance per Rule
            app.setStatus("PENDING_KYC_APPROVAL");

            OutboxEvent event = new OutboxEvent(
                "EVT_" + System.currentTimeMillis(),
                "ACCOUNT_APPLICATION_SUBMITTED",
                "APPLICATION",
                appRef,
                "{\"applicationRef\":\"" + appRef + "\",\"mobile\":\"" + session.getMobile() + "\",\"email\":\"" + session.getEmail() + "\",\"status\":\"PENDING_KYC_APPROVAL\"}"
            );

            boolean saved = dao.saveApplication(app, event);

            if (saved) {
                // Send SES and SNS acknowledgments
                try {
                    AwsEmailSender.send(session.getEmail(), "SK Mini Bank - Application Received (" + appRef + ")",
                        "Dear " + app.getFullName() + ",\n\nYour SK Mini Bank Account Opening Application has been received successfully!\nApplication Reference: " + appRef + "\nStatus: PENDING_KYC_APPROVAL\n\nOur team is reviewing your documents.");
                } catch (Exception ignored) {}

                try {
                    AwsSmsSender.send("+91" + session.getMobile(), "SK Mini Bank: Account Application " + appRef + " submitted successfully. Status: PENDING_KYC_APPROVAL.");
                } catch (Exception ignored) {}

                out.print("{\"status\":\"success\",\"applicationRef\":\"" + appRef + "\",\"message\":\"Application submitted successfully! Your reference is " + appRef + "\"}");
            } else {
                response.setStatus(400);
                out.print("{\"status\":\"failed\",\"message\":\"Application submission failed. Duplicate application detected.\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            out.print("{\"status\":\"error\",\"message\":\"Server Error submitting application.\"}");
        } finally {
            out.flush();
            out.close();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String appRef = request.getParameter("ref");
            if (appRef == null || appRef.trim().isEmpty()) {
                response.setStatus(400);
                out.print("{\"status\":\"failed\",\"message\":\"Application reference required.\"}");
                return;
            }

            AccountApplication app = dao.getApplicationByRef(appRef.trim());
            if (app != null) {
                com.google.gson.JsonObject obj = new com.google.gson.JsonObject();
                obj.addProperty("status", "success");
                obj.addProperty("applicationRef", app.getApplicationRef());
                obj.addProperty("fullName", app.getFullName());
                obj.addProperty("mobile", app.getMobile());
                obj.addProperty("email", app.getEmail());
                obj.addProperty("appStatus", app.getStatus());
                obj.addProperty("aadhaarStatus", app.getAadhaarStatus());
                obj.addProperty("panStatus", app.getPanStatus());
                out.print(obj.toString());
            } else {
                response.setStatus(404);
                out.print("{\"status\":\"failed\",\"message\":\"Application reference not found.\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            out.print("{\"status\":\"error\",\"message\":\"Server error retrieving application status.\"}");
        } finally {
            out.flush();
            out.close();
        }
    }
}
