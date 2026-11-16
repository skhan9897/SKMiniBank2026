package com.bank.service;

import com.bank.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class PaymentRoutingService {

    public static class TransferResult {
        private boolean success;
        private String transactionReference;
        private String status; // SUCCESS, PENDING, FAILED, REVERSED
        private String message;
        private double remainingBalance;

        public TransferResult(boolean success, String transactionReference, String status, String message, double remainingBalance) {
            this.success = success;
            this.transactionReference = transactionReference;
            this.status = status;
            this.message = message;
            this.remainingBalance = remainingBalance;
        }

        public boolean isSuccess() { return success; }
        public String getTransactionReference() { return transactionReference; }
        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public double getRemainingBalance() { return remainingBalance; }
    }

    public synchronized TransferResult transferFunds(int customerId, String sourceAccountNumber, String recipientBankId, 
                                                   String recipientAccountOrVpa, String recipientName, double amount, 
                                                   String idempotencyKey, String transferType) {
        if (amount <= 0) {
            return new TransferResult(false, null, "FAILED", "Transfer amount must be greater than zero.", 0.0);
        }

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            // 1. Idempotency Check
            if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
                try (PreparedStatement ps = con.prepareStatement("SELECT response_payload FROM idempotency_records WHERE idempotency_key = ?")) {
                    ps.setString(1, idempotencyKey);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            con.rollback();
                            return new TransferResult(true, idempotencyKey, "SUCCESS", "Duplicate request handled via idempotency record.", 0.0);
                        }
                    }
                }
            }

            // 2. Verify source account balance and ownership
            double currentBalance = 0.0;
            try (PreparedStatement ps = con.prepareStatement("SELECT wallet_balance FROM customer WHERE id = ? AND account_number = ?")) {
                ps.setInt(1, customerId);
                ps.setString(2, sourceAccountNumber);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        currentBalance = rs.getDouble("wallet_balance");
                    } else {
                        con.rollback();
                        return new TransferResult(false, null, "FAILED", "Source account not authorized or not found.", 0.0);
                    }
                }
            }

            if (currentBalance < amount) {
                con.rollback();
                return new TransferResult(false, null, "FAILED", "Insufficient funds in source account.", currentBalance);
            }

            double newBalance = currentBalance - amount;
            String txRef = "SKTXN-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

            // 3. Deduct balance from source customer
            try (PreparedStatement ps = con.prepareStatement("UPDATE customer SET wallet_balance = ? WHERE id = ?")) {
                ps.setDouble(1, newBalance);
                ps.setInt(2, customerId);
                ps.executeUpdate();
            }

            // 4. Record transaction in ledger
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO transactions (account_number, customer_name, transaction_type, amount, balance, description, status) VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, sourceAccountNumber);
                ps.setString(2, recipientName);
                ps.setString(3, "UNIVERSAL_TRANSFER_" + transferType);
                ps.setDouble(4, amount);
                ps.setDouble(5, newBalance);
                ps.setString(6, "Transfer to " + recipientName + " (" + recipientAccountOrVpa + ") via " + recipientBankId);
                ps.setString(7, "SUCCESS");
                ps.executeUpdate();
            }

            // 5. Store idempotency record if key provided
            if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO idempotency_records (idempotency_key, response_payload) VALUES (?, ?)")) {
                    ps.setString(1, idempotencyKey);
                    ps.setString(2, txRef);
                    ps.executeUpdate();
                }
            }

            // 6. Audit log
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO audit_logs (user_id, bank_id, action, details) VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, customerId);
                ps.setString(2, recipientBankId);
                ps.setString(3, "UNIVERSAL_TRANSFER");
                ps.setString(4, "Transferred " + amount + " to " + recipientAccountOrVpa + " with ref " + txRef);
                ps.executeUpdate();
            }

            con.commit();
            return new TransferResult(true, txRef, "SUCCESS", "Transfer completed successfully.", newBalance);

        } catch (SQLException e) {
            e.printStackTrace();
            return new TransferResult(false, null, "FAILED", "Database error during transfer execution: " + e.getMessage(), 0.0);
        }
    }
}
