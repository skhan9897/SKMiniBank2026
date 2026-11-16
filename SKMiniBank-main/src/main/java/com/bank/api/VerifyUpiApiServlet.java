package com.bank.api;

import com.bank.service.UpiVerificationService;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/api/beneficiaries/verify-upi")
public class VerifyUpiApiServlet extends HttpServlet {
    private final UpiVerificationService upiService = new UpiVerificationService();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        Map reqData = gson.fromJson(sb.toString(), Map.class);
        if (reqData == null) {
            reqData = new HashMap();
            reqData.put("upi_id", request.getParameter("upi_id"));
        }

        String upiId = (String) reqData.get("upi_id");

        UpiVerificationService.UpiVerificationResult result = upiService.verifyUpiId(upiId);

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", result.isSuccess());
        resp.put("status", result.getStatus());
        resp.put("upi_id", result.getVpa());
        resp.put("payee_name", result.getPayeeName());
        resp.put("message", result.getMessage());

        response.getWriter().write(gson.toJson(resp));
    }
}
