package com.bank.api;

import com.bank.util.DBConnection;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/api/setup-db")
public class DatabaseSetupServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/plain");
        StringBuilder logs = new StringBuilder("Starting Complete Database Setup...\n");

        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement()) {
            
            // 1. Ensure users table exists
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "username VARCHAR(100), " +
                         "email VARCHAR(100), " +
                         "password VARCHAR(100), " +
                         "role VARCHAR(20), " +
                         "status VARCHAR(20))");
            logs.append("- Users table verified.\n");

            // 2. Ensure customer table exists
            stmt.execute("CREATE TABLE IF NOT EXISTS customer (" +
                         "customer_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "customer_code VARCHAR(50), " +
                         "cif_number VARCHAR(50), " +
                         "full_name VARCHAR(100), " +
                         "father_name VARCHAR(100), " +
                         "mother_name VARCHAR(100), " +
                         "marital_status VARCHAR(50), " +
                         "dob VARCHAR(20), " +
                         "gender VARCHAR(20), " +
                         "occupation VARCHAR(100), " +
                         "mobile VARCHAR(20) UNIQUE, " +
                         "alternate_mobile VARCHAR(20), " +
                         "email VARCHAR(100), " +
                         "aadhaar VARCHAR(20), " +
                         "pan VARCHAR(20), " +
                         "address TEXT, " +
                         "city VARCHAR(100), " +
                         "state VARCHAR(100), " +
                         "pincode VARCHAR(20), " +
                         "nominee_name VARCHAR(100), " +
                         "relationship VARCHAR(50), " +
                         "nominee_mobile VARCHAR(20), " +
                         "account_number VARCHAR(50) UNIQUE, " +
                         "ifsc_code VARCHAR(20), " +
                         "account_type VARCHAR(50), " +
                         "branch VARCHAR(100), " +
                         "balance DOUBLE DEFAULT 0.0, " +
                         "wallet_balance DOUBLE DEFAULT 0.0, " +
                         "mobile_verified VARCHAR(10) DEFAULT 'NO', " +
                         "email_verified VARCHAR(10) DEFAULT 'NO', " +
                         "upi_id VARCHAR(100), " +
                         "upi_status VARCHAR(20) DEFAULT 'ACTIVE', " +
                         "status VARCHAR(20) DEFAULT 'ACTIVE', " +
                         "kyc_status VARCHAR(20) DEFAULT 'PENDING', " +
                         "password VARCHAR(100), " +
                         "transaction_pin VARCHAR(10), " +
                         "photo VARCHAR(255) DEFAULT 'default_user.png', " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            logs.append("- Customer table verified/created.\n");

            // Add missing columns to customer if created earlier with incomplete schema
            String[][] custColumns = {
                {"photo", "VARCHAR(255) DEFAULT 'default_user.png'"},
                {"mobile_verified", "VARCHAR(10) DEFAULT 'NO'"},
                {"email_verified", "VARCHAR(10) DEFAULT 'NO'"},
                {"upi_id", "VARCHAR(100)"},
                {"upi_status", "VARCHAR(20) DEFAULT 'ACTIVE'"},
                {"transaction_pin", "VARCHAR(10)"},
                {"wallet_balance", "DOUBLE DEFAULT 0.0"}
            };

            for (String[] col : custColumns) {
                if (!columnExists(con, "customer", col[0])) {
                    stmt.execute("ALTER TABLE customer ADD COLUMN " + col[0] + " " + col[1]);
                    logs.append("- Column '").append(col[0]).append("' added to customer.\n");
                }
            }

            // 3. Ensure account table exists
            stmt.execute("CREATE TABLE IF NOT EXISTS account (" +
                         "account_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "customer_id INT, " +
                         "account_number VARCHAR(50) UNIQUE, " +
                         "account_type VARCHAR(50), " +
                         "branch_id INT DEFAULT 1, " +
                         "balance DOUBLE DEFAULT 0.0, " +
                         "status VARCHAR(20) DEFAULT 'ACTIVE', " +
                         "opening_date VARCHAR(50), " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            logs.append("- Account table verified/created.\n");

            // 4. Ensure service_request table exists
            stmt.execute("CREATE TABLE IF NOT EXISTS service_request (" +
                         "request_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "customer_id INT, " +
                         "account_number VARCHAR(50), " +
                         "request_type VARCHAR(50), " +
                         "request_details TEXT, " +
                         "status VARCHAR(20) DEFAULT 'PENDING', " +
                         "remarks TEXT, " +
                         "approved_by VARCHAR(100), " +
                         "approval_date TIMESTAMP NULL, " +
                         "expected_delivery_date DATE NULL, " +
                         "dispatched_date DATE NULL, " +
                         "delivered_date DATE NULL, " +
                         "request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            logs.append("- Service Request table verified/created.\n");

            String[][] srColumns = {
                {"remarks", "TEXT"},
                {"approved_by", "VARCHAR(100)"},
                {"approval_date", "TIMESTAMP NULL"},
                {"expected_delivery_date", "DATE NULL"},
                {"dispatched_date", "DATE NULL"},
                {"delivered_date", "DATE NULL"}
            };

            for (String[] col : srColumns) {
                if (!columnExists(con, "service_request", col[0])) {
                    stmt.execute("ALTER TABLE service_request ADD COLUMN " + col[0] + " " + col[1]);
                    logs.append("- Column '").append(col[0]).append("' added to service_request.\n");
                }
            }

            // 5. Ensure transactions table is created
            stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                         "transaction_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "account_number VARCHAR(50), " +
                         "customer_name VARCHAR(100), " +
                         "transaction_type VARCHAR(50), " +
                         "amount DOUBLE, " +
                         "balance DOUBLE, " +
                         "description TEXT, " +
                         "transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                         "status VARCHAR(20) DEFAULT 'SUCCESS')");
            logs.append("- Transactions table verified/created.\n");

            // 6. Ensure kyc_request, loan_request, atm_request, cheque_book_request, notification tables exist
            stmt.execute("CREATE TABLE IF NOT EXISTS kyc_request (" +
                         "kyc_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "customer_id INT, " +
                         "account_number VARCHAR(50), " +
                         "aadhaar_number VARCHAR(20), " +
                         "pan_number VARCHAR(20), " +
                         "aadhaar_front VARCHAR(255), " +
                         "aadhaar_back VARCHAR(255), " +
                         "pan_image VARCHAR(255), " +
                         "customer_photo VARCHAR(255), " +
                         "signature_image VARCHAR(255), " +
                         "status VARCHAR(20) DEFAULT 'PENDING', " +
                         "remarks TEXT, " +
                         "verified_by VARCHAR(100), " +
                         "request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                         "verification_date TIMESTAMP NULL)");
            logs.append("- KYC Request table verified.\n");

            stmt.execute("CREATE TABLE IF NOT EXISTS loan_request (" +
                         "loan_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "customer_id INT, " +
                         "account_number VARCHAR(50), " +
                         "loan_type VARCHAR(50), " +
                         "loan_amount DOUBLE, " +
                         "status VARCHAR(20) DEFAULT 'PENDING', " +
                         "request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            logs.append("- Loan Request table verified.\n");

            stmt.execute("CREATE TABLE IF NOT EXISTS atm_request (" +
                         "request_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "customer_id INT, " +
                         "account_number VARCHAR(50), " +
                         "card_type VARCHAR(50), " +
                         "request_date VARCHAR(50), " +
                         "status VARCHAR(20) DEFAULT 'PENDING')");
            logs.append("- ATM Request table verified.\n");

            stmt.execute("CREATE TABLE IF NOT EXISTS cheque_book_request (" +
                         "request_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "customer_id INT, " +
                         "account_number VARCHAR(50), " +
                         "pages INT, " +
                         "request_date VARCHAR(50), " +
                         "status VARCHAR(20) DEFAULT 'PENDING')");
            logs.append("- Cheque Book Request table verified.\n");

            stmt.execute("CREATE TABLE IF NOT EXISTS notification (" +
                         "notification_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "customer_id INT, " +
                         "title VARCHAR(100), " +
                         "message TEXT, " +
                         "notification_type VARCHAR(50), " +
                         "status VARCHAR(20) DEFAULT 'SUCCESS', " +
                         "is_read INT DEFAULT 0, " +
                         "action_url VARCHAR(255), " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            logs.append("- Notification table verified.\n");

            // 7. Master Bank Registry tables
            stmt.execute("CREATE TABLE IF NOT EXISTS banks (" +
                         "bank_id VARCHAR(50) PRIMARY KEY, " +
                         "bank_code VARCHAR(20) UNIQUE NOT NULL, " +
                         "bank_name VARCHAR(100) NOT NULL, " +
                         "logo_url VARCHAR(255), " +
                         "routing_prefix VARCHAR(20), " +
                         "status VARCHAR(20) DEFAULT 'ACTIVE', " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            logs.append("- Banks table verified/created.\n");

            stmt.execute("CREATE TABLE IF NOT EXISTS bank_branches (" +
                         "branch_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "bank_id VARCHAR(50), " +
                         "branch_code VARCHAR(20), " +
                         "city VARCHAR(100), " +
                         "ifsc VARCHAR(20) UNIQUE, " +
                         "status VARCHAR(20) DEFAULT 'ACTIVE')");
            logs.append("- Bank Branches table verified/created.\n");

            stmt.execute("CREATE TABLE IF NOT EXISTS bank_user_memberships (" +
                         "membership_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "user_id INT, " +
                         "bank_id VARCHAR(50), " +
                         "role VARCHAR(50), " +
                         "status VARCHAR(20) DEFAULT 'ACTIVE')");
            logs.append("- Bank User Memberships table verified/created.\n");

            stmt.execute("CREATE TABLE IF NOT EXISTS beneficiaries (" +
                         "beneficiary_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "customer_id INT, " +
                         "bank_id VARCHAR(50), " +
                         "account_number VARCHAR(50), " +
                         "ifsc VARCHAR(20), " +
                         "upi_id VARCHAR(100), " +
                         "beneficiary_name VARCHAR(100), " +
                         "status VARCHAR(20) DEFAULT 'VERIFIED', " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            logs.append("- Beneficiaries table verified/created.\n");

            stmt.execute("CREATE TABLE IF NOT EXISTS provider_transactions (" +
                         "provider_tx_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "transaction_id INT, " +
                         "provider_name VARCHAR(50), " +
                         "provider_ref VARCHAR(100), " +
                         "request_payload TEXT, " +
                         "response_payload TEXT, " +
                         "status VARCHAR(30), " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            logs.append("- Provider Transactions table verified/created.\n");

            stmt.execute("CREATE TABLE IF NOT EXISTS idempotency_records (" +
                         "idempotency_key VARCHAR(100) PRIMARY KEY, " +
                         "response_payload TEXT, " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            logs.append("- Idempotency Records table verified/created.\n");

            stmt.execute("CREATE TABLE IF NOT EXISTS audit_logs (" +
                         "audit_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "user_id INT, " +
                         "bank_id VARCHAR(50), " +
                         "action VARCHAR(100), " +
                         "details TEXT, " +
                         "ip_address VARCHAR(50), " +
                         "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            logs.append("- Audit Logs table verified/created.\n");

            // 8. Admin Security & Audit Tables
            stmt.execute("CREATE TABLE IF NOT EXISTS admin (" +
                         "admin_id VARCHAR(50) PRIMARY KEY, " +
                         "username VARCHAR(100) NOT NULL UNIQUE, " +
                         "password_hash VARCHAR(255) NOT NULL, " +
                         "full_name VARCHAR(100) NOT NULL, " +
                         "email VARCHAR(100) NOT NULL UNIQUE, " +
                         "mobile VARCHAR(20) NOT NULL UNIQUE, " +
                         "role VARCHAR(30) NOT NULL DEFAULT 'ADMIN', " +
                         "status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', " +
                         "failed_login_attempts INT DEFAULT 0, " +
                         "locked_until TIMESTAMP NULL, " +
                         "email_verified VARCHAR(10) DEFAULT 'YES', " +
                         "mobile_verified VARCHAR(10) DEFAULT 'YES', " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                         "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                         "last_login_at TIMESTAMP NULL)");
            logs.append("- Admin table verified/created.\n");

            stmt.execute("CREATE TABLE IF NOT EXISTS admin_audit_logs (" +
                         "audit_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "admin_id VARCHAR(50), " +
                         "action VARCHAR(100) NOT NULL, " +
                         "module VARCHAR(50) NOT NULL, " +
                         "target_record_id VARCHAR(100), " +
                         "ip_address VARCHAR(50), " +
                         "result VARCHAR(20) NOT NULL, " +
                         "details TEXT, " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            logs.append("- Admin Audit Logs table verified/created.\n");

            // 9. Enterprise Customer Onboarding, KYC, Outbox & Application Tables
            stmt.execute("CREATE TABLE IF NOT EXISTS customer_otp_verifications (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "identifier VARCHAR(100) NOT NULL, " +
                         "type VARCHAR(20) NOT NULL, " +
                         "otp_hash VARCHAR(255) NOT NULL, " +
                         "attempts INT DEFAULT 0, " +
                         "status VARCHAR(20) DEFAULT 'PENDING', " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                         "expires_at TIMESTAMP NULL)");
            logs.append("- Customer OTP Verifications table verified.\n");

            stmt.execute("CREATE TABLE IF NOT EXISTS customer_registration_sessions (" +
                         "session_token VARCHAR(100) PRIMARY KEY, " +
                         "mobile VARCHAR(20), " +
                         "mobile_verified VARCHAR(10) DEFAULT 'NO', " +
                         "email VARCHAR(100), " +
                         "email_verified VARCHAR(10) DEFAULT 'NO', " +
                         "current_step INT DEFAULT 1, " +
                         "session_data TEXT, " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                         "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)");
            logs.append("- Customer Registration Sessions table verified.\n");

            stmt.execute("CREATE TABLE IF NOT EXISTS account_opening_applications (" +
                         "application_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "application_ref VARCHAR(50) UNIQUE NOT NULL, " +
                         "session_token VARCHAR(100), " +
                         "full_name VARCHAR(100) NOT NULL, " +
                         "father_name VARCHAR(100), " +
                         "mother_name VARCHAR(100), " +
                         "dob VARCHAR(20), " +
                         "gender VARCHAR(20), " +
                         "marital_status VARCHAR(50), " +
                         "occupation VARCHAR(100), " +
                         "mobile VARCHAR(20) NOT NULL, " +
                         "email VARCHAR(100) NOT NULL, " +
                         "aadhaar_number VARCHAR(20), " +
                         "aadhaar_status VARCHAR(50) DEFAULT 'PENDING_CONFIG', " +
                         "aadhaar_ref VARCHAR(100), " +
                         "pan_number VARCHAR(20), " +
                         "pan_status VARCHAR(50) DEFAULT 'PENDING_CONFIG', " +
                         "pan_ref VARCHAR(100), " +
                         "address TEXT, " +
                         "city VARCHAR(100), " +
                         "state VARCHAR(100), " +
                         "pincode VARCHAR(20), " +
                         "account_type VARCHAR(50) DEFAULT 'Savings Account', " +
                         "branch VARCHAR(100) DEFAULT 'Main Digital Branch', " +
                         "initial_balance DOUBLE DEFAULT 0.0, " +
                         "status VARCHAR(30) DEFAULT 'PENDING_KYC_APPROVAL', " +
                         "rejection_reason TEXT, " +
                         "approved_by VARCHAR(100), " +
                         "approved_at TIMESTAMP NULL, " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            logs.append("- Account Opening Applications table verified.\n");

            stmt.execute("CREATE TABLE IF NOT EXISTS nominees (" +
                         "nominee_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "application_ref VARCHAR(50), " +
                         "customer_id INT, " +
                         "nominee_name VARCHAR(100) NOT NULL, " +
                         "relationship VARCHAR(50) NOT NULL, " +
                         "nominee_mobile VARCHAR(20), " +
                         "dob VARCHAR(20), " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            logs.append("- Nominees table verified.\n");

            stmt.execute("CREATE TABLE IF NOT EXISTS customer_kyc (" +
                         "kyc_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "application_ref VARCHAR(50), " +
                         "customer_id INT, " +
                         "aadhaar_status VARCHAR(50), " +
                         "pan_status VARCHAR(50), " +
                         "overall_status VARCHAR(30) DEFAULT 'PENDING_REVIEW', " +
                         "remarks TEXT, " +
                         "reviewed_by VARCHAR(100), " +
                         "reviewed_at TIMESTAMP NULL, " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            logs.append("- Customer KYC table verified.\n");

            stmt.execute("CREATE TABLE IF NOT EXISTS customer_documents (" +
                         "document_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "application_ref VARCHAR(50), " +
                         "customer_id INT, " +
                         "document_type VARCHAR(50) NOT NULL, " +
                         "s3_bucket VARCHAR(100), " +
                         "s3_key VARCHAR(255) NOT NULL, " +
                         "file_name VARCHAR(255) NOT NULL, " +
                         "mime_type VARCHAR(100) NOT NULL, " +
                         "file_size INT, " +
                         "status VARCHAR(20) DEFAULT 'UPLOADED', " +
                         "rejection_reason TEXT, " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            logs.append("- Customer Documents table verified.\n");

            stmt.execute("CREATE TABLE IF NOT EXISTS kyc_verification_logs (" +
                         "log_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "application_ref VARCHAR(50), " +
                         "provider_name VARCHAR(50) NOT NULL, " +
                         "verification_type VARCHAR(50) NOT NULL, " +
                         "reference_id VARCHAR(100), " +
                         "status VARCHAR(30) NOT NULL, " +
                         "response_payload TEXT, " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            logs.append("- KYC Verification Logs table verified.\n");

            stmt.execute("CREATE TABLE IF NOT EXISTS integration_outbox (" +
                         "outbox_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "event_id VARCHAR(100) UNIQUE NOT NULL, " +
                         "event_type VARCHAR(50) NOT NULL, " +
                         "aggregate_type VARCHAR(50) NOT NULL, " +
                         "aggregate_id VARCHAR(100) NOT NULL, " +
                         "payload TEXT NOT NULL, " +
                         "status VARCHAR(20) DEFAULT 'PENDING', " +
                         "retry_count INT DEFAULT 0, " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                         "processed_at TIMESTAMP NULL)");
            logs.append("- Integration Outbox table verified.\n");

            // Provision Super Admin if admin table is empty
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM admin")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String saId = System.getenv("SUPERADMIN_ID") != null ? System.getenv("SUPERADMIN_ID") : "SKMB9897";
                    String saUser = System.getenv("SUPERADMIN_USER") != null ? System.getenv("SUPERADMIN_USER") : "superadmin";
                    String saPass = System.getenv("SUPERADMIN_PASSWORD") != null ? System.getenv("SUPERADMIN_PASSWORD") : "admin123";
                    String saEmail = System.getenv("SUPERADMIN_EMAIL") != null ? System.getenv("SUPERADMIN_EMAIL") : "admin@skminibank.com";
                    String saMobile = System.getenv("SUPERADMIN_MOBILE") != null ? System.getenv("SUPERADMIN_MOBILE") : "+919876543210";
                    String hash = com.bank.util.PasswordUtil.hashPassword(saPass);

                    try (java.sql.PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO admin (admin_id, username, password_hash, full_name, email, mobile, role, status) VALUES (?,?,?,?,?,?,?,?)")) {
                        ps.setString(1, saId);
                        ps.setString(2, saUser);
                        ps.setString(3, hash);
                        ps.setString(4, "System Super Admin");
                        ps.setString(5, saEmail);
                        ps.setString(6, saMobile);
                        ps.setString(7, "SUPER_ADMIN");
                        ps.setString(8, "ACTIVE");
                        ps.executeUpdate();
                    }
                    logs.append("- Seeded Super Admin account (" + saId + ").\n");
                }
            }

            // Seed default banks if empty
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM banks")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.executeUpdate("INSERT INTO banks (bank_id, bank_code, bank_name, routing_prefix, status) VALUES " +
                                       "('SKB', 'SKB001', 'SK Bank of Bareilly', 'SKB', 'ACTIVE'), " +
                                       "('SBI', 'SBIN00', 'State Bank of India', 'SBI', 'ACTIVE'), " +
                                       "('HDFC', 'HDFC00', 'HDFC Bank', 'HDFC', 'ACTIVE'), " +
                                       "('ICICI', 'ICIC00', 'ICICI Bank', 'ICICI', 'ACTIVE'), " +
                                       "('PNB', 'PUNB00', 'Punjab National Bank', 'PNB', 'ACTIVE')");
                    logs.append("- Seeded default supported banks.\n");
                }
            }

            // Seed default branches if empty
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM bank_branches")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.executeUpdate("INSERT INTO bank_branches (bank_id, branch_code, city, ifsc, status) VALUES " +
                                       "('SKB', '001', 'Bareilly', 'SKB0000001', 'ACTIVE'), " +
                                       "('SKB', '002', 'Delhi', 'SKB0000002', 'ACTIVE'), " +
                                       "('SBI', '001', 'Mumbai', 'SBIN0000001', 'ACTIVE'), " +
                                       "('HDFC', '001', 'Delhi', 'HDFC0000001', 'ACTIVE'), " +
                                       "('ICICI', '001', 'Bangalore', 'ICIC0000001', 'ACTIVE'), " +
                                       "('PNB', '001', 'Lucknow', 'PUNB0000001', 'ACTIVE')");
                    logs.append("- Seeded default bank branches and IFSC codes.\n");
                }
            }

            // Seed default customer & user if customer table is empty
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM customer")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.executeUpdate("INSERT INTO customer (" +
                                       "customer_code, cif_number, full_name, father_name, mother_name, marital_status, dob, gender, occupation, " +
                                       "mobile, alternate_mobile, email, aadhaar, pan, address, city, state, pincode, nominee_name, relationship, " +
                                       "nominee_mobile, account_number, ifsc_code, account_type, branch, balance, wallet_balance, mobile_verified, " +
                                       "email_verified, upi_id, upi_status, status, kyc_status, password, transaction_pin, photo" +
                                       ") VALUES (" +
                                       "'SKC1001', 'CIF1001', 'Rahul Sharma', 'Ram Sharma', 'Sita Sharma', 'Single', '1995-05-15', 'Male', 'Engineer', " +
                                       "'9876543210', '9876543211', 'rahul@example.com', '123456789012', 'ABCDE1234F', '123 Civil Lines', 'Bareilly', 'UP', '243001', " +
                                       "'Priya Sharma', 'Sister', '9876543212', 'SKM100200300', 'SKMB0001001', 'Savings Account', 'Bareilly Main Branch', " +
                                       "25000.0, 500.0, 'YES', 'YES', '9876543210@skpay', 'ACTIVE', 'ACTIVE', 'VERIFIED', 'password123', '1234', 'default_user.png'" +
                                       ")");

                    stmt.executeUpdate("INSERT INTO account (customer_id, account_number, account_type, branch_id, balance, status, opening_date) VALUES (" +
                                       "1, 'SKM100200300', 'Savings Account', 1, 25000.0, 'ACTIVE', NOW())");

                    stmt.executeUpdate("INSERT INTO users (username, email, password, role, status) VALUES (" +
                                       "'Rahul Sharma', 'SKM100200300', 'password123', 'CUSTOMER', 'ACTIVE')");

                    logs.append("- Seeded default customer (Mobile: 9876543210, Account: SKM100200300, Password: password123).\n");
                }
            }
            
            logs.append("\nDATABASE SETUP COMPLETED SUCCESSFULLY!");
            response.getWriter().write(logs.toString());

        } catch (Exception e) {
            response.getWriter().write("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean columnExists(Connection con, String tableName, String columnName) {
        try (ResultSet rs = con.getMetaData().getColumns(null, null, tableName, columnName)) {
            return rs.next();
        } catch (Exception e) {
            return false;
        }
    }
}
