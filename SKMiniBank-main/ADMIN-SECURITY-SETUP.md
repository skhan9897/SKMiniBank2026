# SK Mini Bank: Enterprise Admin Security & Multi-Factor Authentication (MFA) Setup Guide

This document details the enterprise-grade Admin Authentication, Dual-OTP Multi-Factor Authentication (MFA), Role-Based Access Control (RBAC), Session Security, Account Lockout, and Audit Logging implementation for SK Mini Bank.

---

## 1. Security Architecture Overview

The SK Mini Bank Admin Command Center is secured via a strict multi-tier authentication flow:

```
[ Step 1: Admin ID & Password ]
             │
             ▼ Validated against MySQL `admin` table & BCrypt hash
[ Check Account Status & Lockout ]
             │
             ▼ Active & Failed attempts < 5
[ Step 2: Trigger Dual-OTP MFA ]
   ├──> AWS SES Email OTP (6-digit) ──> Registered Admin Email
   └──> AWS SNS Mobile SMS OTP (6-digit) ──> Registered Admin Mobile (E.164)
             │
             ▼ User enters BOTH valid OTPs within 5 minutes
[ Validate Both OTPs via OtpChallenge ]
             │
             ▼
[ Promote Session & Enforce RBAC Filter ]
   ├──> SUPER_ADMIN (Level 3): Full Access, Manage Admins, System Config, Audit Logs
   ├──> ADMIN (Level 2): Operations, Customer & Account Approval, Reports
   └──> OPERATOR (Level 1): Read-Only Dashboard & Monitoring
             │
             ▼
[ Admin Command Center Dashboard ]
```

---

## 2. Database Schema & Tables

### 2.1 Admin Table (`admin`)
```sql
CREATE TABLE IF NOT EXISTS admin (
    admin_id VARCHAR(50) PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    mobile VARCHAR(20) NOT NULL UNIQUE,
    role VARCHAR(30) NOT NULL DEFAULT 'ADMIN',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    failed_login_attempts INT DEFAULT 0,
    locked_until TIMESTAMP NULL,
    email_verified VARCHAR(10) DEFAULT 'YES',
    mobile_verified VARCHAR(10) DEFAULT 'YES',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP NULL
);
```

### 2.2 Admin Audit Logs Table (`admin_audit_logs`)
```sql
CREATE TABLE IF NOT EXISTS admin_audit_logs (
    audit_id INT AUTO_INCREMENT PRIMARY KEY,
    admin_id VARCHAR(50),
    action VARCHAR(100) NOT NULL,
    module VARCHAR(50) NOT NULL,
    target_record_id VARCHAR(100),
    ip_address VARCHAR(50),
    result VARCHAR(20) NOT NULL,
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 3. Role-Based Access Control (RBAC)

| Role | Level | Privileges |
| :--- | :--- | :--- |
| **`SUPER_ADMIN`** | 3 | Full administrative access, admin management, role assignment, system configuration, audit log inspection, and banking operations approval. |
| **`ADMIN`** | 2 | Customer management, account opening approvals, KYC verification, ATM/cheque book dispatches, loan approvals, financial reports. |
| **`OPERATOR`** | 1 | Read-only access to dashboard and transaction monitoring. Restricted from performing financial approvals or system configuration changes. |

All administrative servlets and routes are filtered server-side via `AdminAuthFilter.java`.

---

## 4. API Endpoints

- **`POST /AdminLoginServlet`**: Step 1 authentication (Admin ID + Password).
- **`POST /api/admin/otp/email/send`**: Dispatches 6-digit OTP to admin's registered email via AWS SES.
- **`POST /api/admin/otp/mobile/send`**: Dispatches 6-digit OTP to admin's registered mobile via AWS SNS.
- **`POST /api/admin/otp/verify`**: Verifies both Email OTP & Mobile OTP. On dual verification success, completes authentication and issues secure session.
- **`GET /AdminLogoutServlet`**: Logouts admin, invalidates session, and clears OTP session states.

---

## 5. Security & Session Policies

1. **Dual OTP Obligation**: Access to protected admin pages is strictly forbidden without verifying **both** Email and Mobile OTPs.
2. **15-Minute Inactivity Timeout**: Admin sessions expire after 15 minutes of inactivity.
3. **Account Lockout Policy**: 5 consecutive invalid password attempts automatically lock the admin account for 15 minutes.
4. **Resend Cooldown**: A 60-second cooldown is enforced between OTP resend requests.
5. **Anti-Replay & Token Bound**: OTPs expire after 5 minutes (300s) and are invalidated immediately upon max attempts (5) or successful verification.

---

## 6. Environment Variables (`.env.example`)

```env
ADMIN_OTP_EXPIRY_SECONDS=300
ADMIN_OTP_MAX_ATTEMPTS=5
ADMIN_OTP_RESEND_COOLDOWN_SECONDS=60
ADMIN_SESSION_TIMEOUT_SECONDS=900
ADMIN_MAX_LOGIN_ATTEMPTS=5
ADMIN_LOCK_DURATION_MINUTES=15

SUPERADMIN_ID=SKMB9897
SUPERADMIN_USER=superadmin
SUPERADMIN_PASSWORD=change_this_secure_password_in_production
SUPERADMIN_EMAIL=admin@skminibank.com
SUPERADMIN_MOBILE=+919876543210

AWS_REGION=ap-south-1
AWS_SES_FROM_EMAIL=verified-sender@example.com
```

---

## 7. Verification & Operational Testing

- **Password Verification Test**: Enter correct Admin ID & Password. Verify step 2 (Dual OTP MFA screen) appears.
- **Lockout Test**: Enter wrong password 5 times. Verify account lock message appears.
- **Dual OTP Verification Test**: Enter valid Email OTP & Mobile OTP. Verify redirection to Admin Command Center (`/DashboardServlet`).
- **Session Timeout Test**: Leave admin portal idle for 15 minutes. Verify session expires and redirects to login with error.
