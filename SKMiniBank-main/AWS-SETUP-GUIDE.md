# SK Mini Bank: AWS Services Setup & Integration Guide

This guide details the integration of AWS Simple Email Service (SES), AWS Simple Notification Service (SNS), and AWS Simple Storage Service (S3) within SK Mini Bank.

---

## 1. AWS SES (Simple Email Service)

### Functionality
- Customer Registration Email OTP (Step 2)
- Admin Login Dual-OTP MFA Email (Step 2)
- Application Submission Acknowledgement
- Admin KYC Approval & Rejection Notifications

### Environment Variables
```env
AWS_REGION=ap-south-1
AWS_SES_FROM_EMAIL=verified-sender@skminibank.com
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key
```

---

## 2. AWS SNS (Simple Notification Service)

### Functionality
- Customer Mobile SMS OTP (Step 1)
- Admin Login Dual-OTP MFA SMS (Step 2)
- Real-time Account Activation SMS Notifications

### SMS E.164 Format
All mobile numbers are formatted automatically to E.164 standard (`+919876543210`).

---

## 3. AWS S3 (Simple Storage Service)

### Functionality
- Encrypted private document storage for Photo, Aadhaar, PAN, and Signature uploads.
- Server-side encryption at rest (`AES256`).

### Environment Variable
```env
AWS_S3_BUCKET=skminibank-private-documents
```
