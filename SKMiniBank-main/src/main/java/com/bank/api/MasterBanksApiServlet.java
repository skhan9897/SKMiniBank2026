package com.bank.api;

import com.bank.util.DBConnection;
import com.google.gson.Gson;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/api/master/banks")
public class MasterBanksApiServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        Gson gson = new Gson();
        List<Map<String, Object>> banks = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT bank_id, bank_code, bank_name, logo_url, routing_prefix, status FROM banks WHERE status = 'ACTIVE'")) {
            
            while (rs.next()) {
                Map<String, Object> bank = new HashMap<>();
                bank.put("bank_id", rs.getString("bank_id"));
                bank.put("bank_code", rs.getString("bank_code"));
                bank.put("bank_name", rs.getString("bank_name"));
                bank.put("logo_url", rs.getString("logo_url"));
                bank.put("routing_prefix", rs.getString("routing_prefix"));
                bank.put("status", rs.getString("status"));
                banks.add(bank);
            }

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("banks", banks);
            response.getWriter().write(gson.toJson(resp));

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            response.getWriter().write(gson.toJson(err));
        }
    }
}
