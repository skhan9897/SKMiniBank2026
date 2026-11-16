package com.bank.api;

import com.bank.service.AccountVerificationService;
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

@WebServlet("/api/beneficiaries/verify-mobile")
public class VerifyMobileApiServlet extends HttpServlet {
    private final AccountVerificationService verificationService = new AccountVerificationService();
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
            reqData.put("bank_id", request.getParameter("bank_id"));
            reqData.put("mobile", request.getParameter("mobile"));
        }

        String bankId = (String) reqData.get("bank_id");
        String mobile = (String) reqData.get("mobile");

        AccountVerificationService.VerificationResult result = verificationService.verifyMobile(bankId, mobile);

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", result.isSuccess());
        resp.put("status", result.getStatus());
        resp.put("beneficiary_name", result.getBeneficiaryName());
        resp.put("account_number", result.getAccountNumber());
        resp.put("ifsc", result.getIfsc());
        resp.put("message", result.getMessage());
        resp.put("bank_id", result.getBankId());

        response.getWriter().write(gson.toJson(resp));
    }
}
