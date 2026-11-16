# SK Mini Bank: Authorized Aadhaar & PAN Provider Integration Guide

This document outlines the UIDAI Aadhaar and NSDL/UTI PAN provider adapters and security policies.

---

## 1. Authorized Aadhaar Provider Policy

- Direct unauthorized UIDAI API calls are strictly forbidden.
- Verification uses authorized UIDAI-compliant provider APIs (`ConfiguredAadhaarOtpProvider.java`).
- If provider credentials (`AADHAAR_PROVIDER_ENDPOINT`, `AADHAAR_PROVIDER_CLIENT_ID`, `AADHAAR_PROVIDER_CLIENT_SECRET`) are not set, the system safely reports:
  `"Aadhaar Verification Pending Configuration"`
- Aadhaar numbers are masked in UI and log files (`XXXX-XXXX-1234`).

---

## 2. Authorized PAN Provider Policy

- Format checking alone does not mark a PAN as verified.
- Real-time verification uses authorized NSDL/UTI provider APIs (`ConfiguredPanVerificationProvider.java`).
- If provider credentials (`PAN_PROVIDER_BASE_URL`, `PAN_PROVIDER_CLIENT_ID`, `PAN_PROVIDER_CLIENT_SECRET`) are unconfigured, the system reports:
  `"PAN Verification Pending Configuration"`

---

## 3. Environment Configuration

```env
AADHAAR_PROVIDER_ENDPOINT=https://provider.api.com/aadhaar
AADHAAR_PROVIDER_CLIENT_ID=your_client_id
AADHAAR_PROVIDER_CLIENT_SECRET=your_client_secret

PAN_PROVIDER_BASE_URL=https://provider.api.com/pan
PAN_PROVIDER_CLIENT_ID=your_client_id
PAN_PROVIDER_CLIENT_SECRET=your_client_secret
```
