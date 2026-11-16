package com.bank.api;

import com.bank.dao.OnboardingDAO;
import com.bank.model.AccountApplication;
import com.bank.model.CustomerDocument;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/api/admin/applications")
public class AdminApplicationsServlet extends HttpServlet {

    private final OnboardingDAO dao = new OnboardingDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String appRef = request.getParameter("ref");

            if (appRef != null && !appRef.trim().isEmpty()) {
                AccountApplication app = dao.getApplicationByRef(appRef.trim());
                if (app == null) {
                    response.setStatus(404);
                    out.print("{\"status\":\"failed\",\"message\":\"Application not found.\"}");
                    return;
                }

                List<CustomerDocument> docs = dao.getDocumentsByAppRef(appRef.trim());

                JsonObject result = new JsonObject();
                result.addProperty("status", "success");
                result.add("application", gson.toJsonTree(app));
                result.add("documents", gson.toJsonTree(docs));

                out.print(result.toString());

            } else {
                List<AccountApplication> list = dao.getAllApplications();
                JsonObject result = new JsonObject();
                result.addProperty("status", "success");
                result.add("applications", gson.toJsonTree(list));
                out.print(result.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            out.print("{\"status\":\"error\",\"message\":\"Server error retrieving applications.\"}");
        } finally {
            out.flush();
            out.close();
        }
    }
}
