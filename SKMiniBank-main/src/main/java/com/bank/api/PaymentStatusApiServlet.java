package com.bank.api;

import com.bank.util.DBConnection;
import com.google.gson.Gson;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/api/payments/status")
public class PaymentStatusApiServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        Gson gson = new Gson();
        String ref = request.getParameter("reference");

        if (ref == null || ref.trim().isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Transaction reference is required.");
            response.getWriter().write(gson.toJson(err));
            return;
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM transactions WHERE description LIKE ?")) {
            ps.setString(1, "%" + ref + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> tx = new HashMap<>();
                    tx.put("transaction_id", rs.getInt("transaction_id"));
                    tx.put("account_number", rs.getString("account_number"));
                    tx.put("customer_name", rs.getString("customer_name"));
                    tx.put("transaction_type", rs.getString("transaction_type"));
                    tx.put("amount", rs.getDouble("amount"));
                    tx.put("balance", rs.getDouble("balance"));
                    tx.put("description", rs.getString("description"));
                    tx.put("transaction_date", rs.getString("transaction_date"));
                    tx.put("status", rs.getString("status"));

                    Map<String, Object> resp = new HashMap<>();
                    resp.put("success", true);
                    resp.put("transaction", tx);
                    response.getWriter().write(gson.toJson(resp));
                } else {
                    Map<String, Object> resp = new HashMap<>();
                    resp.put("success", false);
                    resp.put("message", "Transaction reference not found.");
                    response.getWriter().write(gson.toJson(resp));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            response.getWriter().write(gson.toJson(err));
        }
    }
}
