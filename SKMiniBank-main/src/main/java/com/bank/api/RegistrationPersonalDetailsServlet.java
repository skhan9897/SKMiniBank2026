package com.bank.api;

import com.bank.dao.OnboardingDAO;
import com.bank.model.OnboardingSession;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/api/registration/personal-details")
public class RegistrationPersonalDetailsServlet extends HttpServlet {

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
                out.print("{\"status\":\"failed\",\"message\":\"Mobile and Email verification required first.\"}");
                return;
            }

            String fullName = request.getParameter("fullName");
            String fatherName = request.getParameter("fatherName");
            String motherName = request.getParameter("motherName");
            String dob = request.getParameter("dob");
            String gender = request.getParameter("gender");
            String maritalStatus = request.getParameter("maritalStatus");
            String occupation = request.getParameter("occupation");
            String address = request.getParameter("address");
            String city = request.getParameter("city");
            String state = request.getParameter("state");
            String pincode = request.getParameter("pincode");

            if (fullName == null || dob == null || address == null || city == null || state == null || pincode == null) {
                response.setStatus(400);
                out.print("{\"status\":\"failed\",\"message\":\"Missing required personal detail fields.\"}");
                return;
            }

            com.google.gson.JsonObject json = new com.google.gson.JsonObject();
            json.addProperty("fullName", fullName);
            json.addProperty("fatherName", fatherName);
            json.addProperty("motherName", motherName);
            json.addProperty("dob", dob);
            json.addProperty("gender", gender);
            json.addProperty("maritalStatus", maritalStatus);
            json.addProperty("occupation", occupation);
            json.addProperty("address", address);
            json.addProperty("city", city);
            json.addProperty("state", state);
            json.addProperty("pincode", pincode);

            session.setCurrentStep(4);
            session.setSessionData(json.toString());
            dao.saveSession(session);

            out.print("{\"status\":\"success\",\"message\":\"Personal details saved successfully!\"}");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            out.print("{\"status\":\"error\",\"message\":\"Server Error saving personal details.\"}");
        } finally {
            out.flush();
            out.close();
        }
    }
}
