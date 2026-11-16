# SK Mini Bank: Complete Customer Onboarding & KYC Operational Guide

This document provides the operational walkthrough for the 10-Step Account Opening Wizard and Admin KYC Approval process.

---

## 1. 10-Step Customer Wizard Journey

1. **Mobile Verification**: User enters 10-digit Indian mobile number. System sends 6-digit AWS SNS OTP and verifies it on the server.
2. **Email Verification**: User enters email address. System sends 6-digit AWS SES OTP and verifies it on the server.
3. **Personal Details**: Collects Full Name, Father's Name, Mother's Name, DOB, Gender, Marital Status, Occupation, Address, City, State, Pincode.
4. **Aadhaar Verification**: Entry of 12-digit Aadhaar number with user consent and authorized provider check.
5. **PAN Verification**: Entry of 10-character PAN number and authorized provider check.
6. **Document Upload**: Photo, Aadhaar, PAN, and Signature uploaded securely to encrypted AWS S3 storage.
7. **Nominee Details**: Nominee Name, Relationship, Mobile, DOB.
8. **Account Selection**: Savings or Current Account selection with zero initial balance notice.
9. **Final Review**: Comprehensive review of all inputs before submission.
10. **Application Confirmation**: Issues unique Application Reference (`SKM-APP-xxxx`) with status `PENDING_KYC_APPROVAL`.

---

## 2. Admin KYC Approval & Account Activation

1. Admin logs into Command Center (`/SKMiniBankadmin-login.jsp`) using password + dual OTP MFA.
2. Admin opens Applications / KYC dashboard (`/api/admin/applications`).
3. Admin inspects uploaded documents and identity verification statuses.
4. Admin clicks **Approve Application**:
   - System assigns unique Account Number (`SKMxxxx`), CIF, IFSC.
   - Initial balance is set to **₹0.00**.
   - Account status is set to **`ACTIVE`**.
   - AWS SES/SNS notifications dispatched.
   - `ACCOUNT_ACTIVATED` event published via Kafka outbox.
