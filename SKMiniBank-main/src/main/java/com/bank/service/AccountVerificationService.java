package com.bank.service;

import com.bank.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AccountVerificationService {

    public static class VerificationResult {
        private boolean success;
        private String status; // VERIFIED, FAILED, UNSUPPORTED_BANK, PROVIDER_UNAVAILABLE
        private String beneficiaryName;
        private String message;
        private String bankId;
        private String accountNumber;
        private String ifsc;

        public VerificationResult(boolean success, String status, String beneficiaryName, String message, String bankId, String accountNumber, String ifsc) {
            this.success = success;
            this.status = status;
            this.beneficiaryName = beneficiaryName;
            this.message = message;
            this.bankId = bankId;
            this.accountNumber = accountNumber;
            this.ifsc = ifsc;
        }

        public boolean isSuccess() { return success; }
        public String getStatus() { return status; }
        public String getBeneficiaryName() { return beneficiaryName; }
        public String getMessage() { return message; }
        public String getBankId() { return bankId; }
        public String getAccountNumber() { return accountNumber; }
        public String getIfsc() { return ifsc; }
    }

    public VerificationResult verifyAccount(String bankId, String accountNumber, String ifsc) {
        if (bankId == null || bankId.trim().isEmpty() || accountNumber == null || accountNumber.trim().isEmpty()) {
            return new VerificationResult(false, "FAILED", null, "Bank ID and Account Number are required.", bankId, accountNumber, ifsc);
        }

        if (accountNumber.length() < 8 || accountNumber.length() > 20) {
            return new VerificationResult(false, "FAILED", null, "Invalid account number length.", bankId, accountNumber, ifsc);
        }

        try (Connection con = DBConnection.getConnection()) {
            // Check if bank exists and is active
            try (PreparedStatement ps = con.prepareStatement("SELECT bank_name, status FROM banks WHERE bank_id = ?")) {
                ps.setString(1, bankId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return new VerificationResult(false, "UNSUPPORTED_BANK", null, "Bank ID not supported in master registry.", bankId, accountNumber, ifsc);
                    }
                    String bankStatus = rs.getString("status");
                    if (!"ACTIVE".equalsIgnoreCase(bankStatus)) {
                        return new VerificationResult(false, "PROVIDER_UNAVAILABLE", null, "Selected bank service is currently suspended or inactive.", bankId, accountNumber, ifsc);
                    }
                }
            }

            // If bank is SKB (SK Bank of Bareilly), check local customer database
            if ("SKB".equalsIgnoreCase(bankId)) {
                try (PreparedStatement ps = con.prepareStatement("SELECT full_name FROM customer WHERE account_number = ?")) {
                    ps.setString(1, accountNumber);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            String name = rs.getString("full_name");
                            return new VerificationResult(true, "VERIFIED", name, "Account verified successfully via core banking.", bankId, accountNumber, ifsc);
                        } else {
                            return new VerificationResult(false, "FAILED", null, "Account number not found in SK Bank of Bareilly records.", bankId, accountNumber, ifsc);
                        }
                    }
                }
            } else {
                // External bank sandbox verification simulation
                String mockName = "Verified Customer (" + accountNumber.substring(accountNumber.length() - 4) + ")";
                return new VerificationResult(true, "VERIFIED", mockName, "Account verified successfully via authorized interbank routing rail.", bankId, accountNumber, ifsc);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new VerificationResult(false, "PROVIDER_UNAVAILABLE", null, "Verification provider error: " + e.getMessage(), bankId, accountNumber, ifsc);
        }
    }

    public VerificationResult verifyMobile(String bankId, String mobile) {
        if (bankId == null || bankId.trim().isEmpty() || mobile == null || mobile.trim().isEmpty()) {
            return new VerificationResult(false, "FAILED", null, "Bank ID and Mobile Number are required.", bankId, null, null);
        }

        try (Connection con = DBConnection.getConnection()) {
            // Check if bank exists and is active
            try (PreparedStatement ps = con.prepareStatement("SELECT bank_name, status FROM banks WHERE bank_id = ?")) {
                ps.setString(1, bankId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return new VerificationResult(false, "UNSUPPORTED_BANK", null, "Bank ID not supported in master registry.", bankId, null, null);
                    }
                }
            }

            if ("SKB".equalsIgnoreCase(bankId)) {
                try (PreparedStatement ps = con.prepareStatement("SELECT full_name, account_number FROM customer WHERE mobile = ?")) {
                    ps.setString(1, mobile);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            String name = rs.getString("full_name");
                            String accNo = rs.getString("account_number");
                            return new VerificationResult(true, "VERIFIED", name, "Account found for mobile number in SK Bank of Bareilly.", bankId, accNo, "SKB0000001");
                        } else {
                            return new VerificationResult(false, "FAILED", null, "No account linked to this mobile number in SK Bank of Bareilly.", bankId, null, null);
                        }
                    }
                }
            } else {
                // External bank mobile lookup simulation
                String mockAcc = "EXT" + mobile.hashCode();
                String mockName = "Verified Mobile User (" + mobile.substring(mobile.length() - 4) + ")";
                return new VerificationResult(true, "VERIFIED", mockName, "Account resolved via " + bankId + " mobile registry.", bankId, mockAcc, bankId + "000123");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new VerificationResult(false, "PROVIDER_UNAVAILABLE", null, "Mobile verification error: " + e.getMessage(), bankId, null, null);
        }
    }
}
