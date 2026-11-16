package com.bank.api;

import com.bank.service.PaymentRoutingService;
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
import javax.servlet.http.HttpSession;

@WebServlet("/api/payments/transfer")
public class UniversalTransferApiServlet extends HttpServlet {
    private final PaymentRoutingService paymentService = new PaymentRoutingService();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");

        HttpSession session = request.getSession(false);
        Integer customerId = session != null ? (Integer) session.getAttribute("customer_id") : null;
        String sourceAccountNumber = session != null ? (String) session.getAttribute("account_number") : null;

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
        }

        // Allow fallback customer_id/account_number from request body for API clients (like Android app)
        if (customerId == null && reqData.containsKey("customer_id")) {
            customerId = ((Number) reqData.get("customer_id")).intValue();
        }
        if (sourceAccountNumber == null && reqData.containsKey("source_account_number")) {
            sourceAccountNumber = (String) reqData.get("source_account_number");
        }

        if (customerId == null || sourceAccountNumber == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Unauthorized or missing customer session/credentials.");
            response.getWriter().write(gson.toJson(err));
            return;
        }

        String recipientBankId = (String) reqData.get("recipient_bank_id");
        String recipientAccountOrVpa = (String) reqData.get("recipient_account_or_vpa");
        String recipientName = (String) reqData.get("recipient_name");
        double amount = reqData.containsKey("amount") ? ((Number) reqData.get("amount")).doubleValue() : 0.0;
        String idempotencyKey = (String) reqData.get("idempotency_key");
        String transferType = reqData.containsKey("transfer_type") ? (String) reqData.get("transfer_type") : "ACCOUNT";

        PaymentRoutingService.TransferResult result = paymentService.transferFunds(
                customerId, sourceAccountNumber, recipientBankId, recipientAccountOrVpa, recipientName, amount, idempotencyKey, transferType
        );

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", result.isSuccess());
        resp.put("transaction_reference", result.getTransactionReference());
        resp.put("status", result.getStatus());
        resp.put("message", result.getMessage());
        resp.put("remaining_balance", result.getRemainingBalance());

        response.getWriter().write(gson.toJson(resp));
    }
}
