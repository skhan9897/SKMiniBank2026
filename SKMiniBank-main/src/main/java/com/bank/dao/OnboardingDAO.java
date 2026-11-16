package com.bank.dao;

import com.bank.model.AccountApplication;
import com.bank.model.CustomerDocument;
import com.bank.model.OnboardingSession;
import com.bank.model.OutboxEvent;
import com.bank.util.DBConnection;
import com.bank.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OnboardingDAO {

    public boolean saveSession(OnboardingSession session) {
        String sql = "INSERT INTO customer_registration_sessions (session_token, mobile, mobile_verified, email, email_verified, current_step, session_data) " +
                     "VALUES (?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE mobile=?, mobile_verified=?, email=?, email_verified=?, current_step=?, session_data=?, updated_at=NOW()";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, session.getSessionToken());
            ps.setString(2, session.getMobile());
            ps.setString(3, session.getMobileVerified());
            ps.setString(4, session.getEmail());
            ps.setString(5, session.getEmailVerified());
            ps.setInt(6, session.getCurrentStep());
            ps.setString(7, session.getSessionData());

            ps.setString(8, session.getMobile());
            ps.setString(9, session.getMobileVerified());
            ps.setString(10, session.getEmail());
            ps.setString(11, session.getEmailVerified());
            ps.setInt(12, session.getCurrentStep());
            ps.setString(13, session.getSessionData());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public OnboardingSession getSession(String token) {
        if (token == null || token.trim().isEmpty()) return null;
        String sql = "SELECT * FROM customer_registration_sessions WHERE session_token = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    OnboardingSession s = new OnboardingSession();
                    s.setSessionToken(rs.getString("session_token"));
                    s.setMobile(rs.getString("mobile"));
                    s.setMobileVerified(rs.getString("mobile_verified"));
                    s.setEmail(rs.getString("email"));
                    s.setEmailVerified(rs.getString("email_verified"));
                    s.setCurrentStep(rs.getInt("current_step"));
                    s.setSessionData(rs.getString("session_data"));
                    s.setCreatedAt(rs.getTimestamp("created_at"));
                    s.setUpdatedAt(rs.getTimestamp("updated_at"));
                    return s;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isMobileRegistered(String mobile) {
        if (mobile == null) return false;
        String sql = "SELECT COUNT(*) FROM customer WHERE mobile = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, mobile.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isEmailRegistered(String email) {
        if (email == null) return false;
        String sql = "SELECT COUNT(*) FROM customer WHERE email = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean saveApplication(AccountApplication app, OutboxEvent event) {
        String sqlApp = "INSERT INTO account_opening_applications (" +
                "application_ref, session_token, full_name, father_name, mother_name, dob, gender, marital_status, occupation, " +
                "mobile, email, aadhaar_number, aadhaar_status, pan_number, pan_status, address, city, state, pincode, account_type, branch, initial_balance, status" +
                ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(sqlApp)) {
                ps.setString(1, app.getApplicationRef());
                ps.setString(2, app.getSessionToken());
                ps.setString(3, app.getFullName());
                ps.setString(4, app.getFatherName());
                ps.setString(5, app.getMotherName());
                ps.setString(6, app.getDob());
                ps.setString(7, app.getGender());
                ps.setString(8, app.getMaritalStatus());
                ps.setString(9, app.getOccupation());
                ps.setString(10, app.getMobile());
                ps.setString(11, app.getEmail());
                ps.setString(12, app.getAadhaarNumber());
                ps.setString(13, app.getAadhaarStatus());
                ps.setString(14, app.getPanNumber());
                ps.setString(15, app.getPanStatus());
                ps.setString(16, app.getAddress());
                ps.setString(17, app.getCity());
                ps.setString(18, app.getState());
                ps.setString(19, app.getPincode());
                ps.setString(20, app.getAccountType() != null ? app.getAccountType() : "Savings Account");
                ps.setString(21, app.getBranch() != null ? app.getBranch() : "Main Digital Branch");
                ps.setDouble(22, 0.0); // Zero Initial Balance per Rule
                ps.setString(23, "PENDING_KYC_APPROVAL");
                ps.executeUpdate();
            }

            if (event != null) {
                new OutboxDAO().saveEvent(con, event);
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public AccountApplication getApplicationByRef(String appRef) {
        if (appRef == null) return null;
        String sql = "SELECT * FROM account_opening_applications WHERE application_ref = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, appRef);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapAppRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<AccountApplication> getAllApplications() {
        List<AccountApplication> list = new ArrayList<>();
        String sql = "SELECT * FROM account_opening_applications ORDER BY created_at DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapAppRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean saveDocument(CustomerDocument doc) {
        String sql = "INSERT INTO customer_documents (application_ref, customer_id, document_type, s3_bucket, s3_key, file_name, mime_type, file_size, status) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, doc.getApplicationRef());
            ps.setInt(2, doc.getCustomerId());
            ps.setString(3, doc.getDocumentType());
            ps.setString(4, doc.getS3Bucket());
            ps.setString(5, doc.getS3Key());
            ps.setString(6, doc.getFileName());
            ps.setString(7, doc.getMimeType());
            ps.setInt(8, doc.getFileSize());
            ps.setString(9, "UPLOADED");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<CustomerDocument> getDocumentsByAppRef(String appRef) {
        List<CustomerDocument> list = new ArrayList<>();
        String sql = "SELECT * FROM customer_documents WHERE application_ref = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, appRef);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CustomerDocument d = new CustomerDocument();
                    d.setDocumentId(rs.getInt("document_id"));
                    d.setApplicationRef(rs.getString("application_ref"));
                    d.setCustomerId(rs.getInt("customer_id"));
                    d.setDocumentType(rs.getString("document_type"));
                    d.setS3Bucket(rs.getString("s3_bucket"));
                    d.setS3Key(rs.getString("s3_key"));
                    d.setFileName(rs.getString("file_name"));
                    d.setMimeType(rs.getString("mime_type"));
                    d.setFileSize(rs.getInt("file_size"));
                    d.setStatus(rs.getString("status"));
                    d.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(d);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean approveApplicationAndCreateAccount(String appRef, String adminId) {
        AccountApplication app = getApplicationByRef(appRef);
        if (app == null || !"PENDING_KYC_APPROVAL".equalsIgnoreCase(app.getStatus())) return false;

        String accNo = "SKM" + (long)(Math.random() * 10000000000L);
        String cif = "CIF" + System.currentTimeMillis();
        String customerCode = "SKC" + System.currentTimeMillis();
        String defaultPass = "Password@123";
        String defaultPin = "1234";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            // 1. Insert into Customer Table
            String sqlCust = "INSERT INTO customer (customer_code, cif_number, full_name, father_name, mother_name, marital_status, dob, gender, occupation, " +
                    "mobile, email, aadhaar, pan, address, city, state, pincode, account_number, ifsc_code, account_type, branch, balance, wallet_balance, " +
                    "mobile_verified, email_verified, upi_id, upi_status, status, kyc_status, password, transaction_pin) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            int customerId = 0;
            try (PreparedStatement ps = con.prepareStatement(sqlCust, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, customerCode);
                ps.setString(2, cif);
                ps.setString(3, app.getFullName());
                ps.setString(4, app.getFatherName());
                ps.setString(5, app.getMotherName());
                ps.setString(6, app.getMaritalStatus());
                ps.setString(7, app.getDob());
                ps.setString(8, app.getGender());
                ps.setString(9, app.getOccupation());
                ps.setString(10, app.getMobile());
                ps.setString(11, app.getEmail());
                ps.setString(12, app.getAadhaarNumber());
                ps.setString(13, app.getPanNumber());
                ps.setString(14, app.getAddress());
                ps.setString(15, app.getCity());
                ps.setString(16, app.getState());
                ps.setString(17, app.getPincode());
                ps.setString(18, accNo);
                ps.setString(19, "SKMB0001001");
                ps.setString(20, app.getAccountType());
                ps.setString(21, app.getBranch());
                ps.setDouble(22, 0.0); // Zero Initial Balance
                ps.setDouble(23, 0.0);
                ps.setString(24, "YES");
                ps.setString(25, "YES");
                ps.setString(26, app.getMobile() + "@skpay");
                ps.setString(27, "ACTIVE");
                ps.setString(28, "ACTIVE");
                ps.setString(29, "APPROVED");
                ps.setString(30, defaultPass);
                ps.setString(31, defaultPin);
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) customerId = rs.getInt(1);
                }
            }

            // 2. Insert into Account Table
            String sqlAcc = "INSERT INTO account (customer_id, account_number, account_type, branch_id, balance, status, opening_date) VALUES (?,?,?,?,?,?,NOW())";
            try (PreparedStatement ps = con.prepareStatement(sqlAcc)) {
                ps.setInt(1, customerId);
                ps.setString(2, accNo);
                ps.setString(3, app.getAccountType());
                ps.setInt(4, 1);
                ps.setDouble(5, 0.0);
                ps.setString(6, "ACTIVE");
                ps.executeUpdate();
            }

            // 3. Update Application Status
            String sqlAppUpdate = "UPDATE account_opening_applications SET status = 'APPROVED', approved_by = ?, approved_at = NOW() WHERE application_ref = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlAppUpdate)) {
                ps.setString(1, adminId);
                ps.setString(2, appRef);
                ps.executeUpdate();
            }

            // 4. Save Outbox Event
            OutboxEvent event = new OutboxEvent(
                "EVT_" + System.currentTimeMillis(),
                "ACCOUNT_ACTIVATED",
                "ACCOUNT",
                accNo,
                "{\"applicationRef\":\"" + appRef + "\",\"accountNumber\":\"" + accNo + "\",\"status\":\"ACTIVATED\"}"
            );
            new OutboxDAO().saveEvent(con, event);

            con.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean rejectApplication(String appRef, String adminId, String reason) {
        String sql = "UPDATE account_opening_applications SET status = 'REJECTED', rejection_reason = ?, approved_by = ?, approved_at = NOW() WHERE application_ref = ?";
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, reason);
                ps.setString(2, adminId);
                ps.setString(3, appRef);
                ps.executeUpdate();
            }
            OutboxEvent event = new OutboxEvent(
                "EVT_" + System.currentTimeMillis(),
                "KYC_REJECTED",
                "APPLICATION",
                appRef,
                "{\"applicationRef\":\"" + appRef + "\",\"status\":\"REJECTED\",\"reason\":\"" + (reason != null ? reason : "KYC Document rejection") + "\"}"
            );
            new OutboxDAO().saveEvent(con, event);
            con.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private AccountApplication mapAppRow(ResultSet rs) throws SQLException {
        AccountApplication a = new AccountApplication();
        a.setApplicationId(rs.getInt("application_id"));
        a.setApplicationRef(rs.getString("application_ref"));
        a.setSessionToken(rs.getString("session_token"));
        a.setFullName(rs.getString("full_name"));
        a.setFatherName(rs.getString("father_name"));
        a.setMotherName(rs.getString("mother_name"));
        a.setDob(rs.getString("dob"));
        a.setGender(rs.getString("gender"));
        a.setMaritalStatus(rs.getString("marital_status"));
        a.setOccupation(rs.getString("occupation"));
        a.setMobile(rs.getString("mobile"));
        a.setEmail(rs.getString("email"));
        a.setAadhaarNumber(rs.getString("aadhaar_number"));
        a.setAadhaarStatus(rs.getString("aadhaar_status"));
        a.setAadhaarRef(rs.getString("aadhaar_ref"));
        a.setPanNumber(rs.getString("pan_number"));
        a.setPanStatus(rs.getString("pan_status"));
        a.setPanRef(rs.getString("pan_ref"));
        a.setAddress(rs.getString("address"));
        a.setCity(rs.getString("city"));
        a.setState(rs.getString("state"));
        a.setPincode(rs.getString("pincode"));
        a.setAccountType(rs.getString("account_type"));
        a.setBranch(rs.getString("branch"));
        a.setInitialBalance(rs.getDouble("initial_balance"));
        a.setStatus(rs.getString("status"));
        a.setRejectionReason(rs.getString("rejection_reason"));
        a.setApprovedBy(rs.getString("approved_by"));
        a.setApprovedAt(rs.getTimestamp("approved_at"));
        a.setCreatedAt(rs.getTimestamp("created_at"));
        return a;
    }
}
