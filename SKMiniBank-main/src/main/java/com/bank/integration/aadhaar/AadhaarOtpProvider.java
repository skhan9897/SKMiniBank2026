package com.bank.integration.aadhaar;

/** Adapter contract for a UIDAI-authorized Aadhaar authentication provider. */
public interface AadhaarOtpProvider {
    String requestOtp(String aadhaarNumber, String consentReference);
    boolean verifyOtp(String transactionId, String otp);
}
