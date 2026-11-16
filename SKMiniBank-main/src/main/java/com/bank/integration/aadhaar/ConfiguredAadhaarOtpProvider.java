package com.bank.integration.aadhaar;

/** Fails closed until an authorized provider adapter and credentials are configured. */
public final class ConfiguredAadhaarOtpProvider implements AadhaarOtpProvider {
    private static boolean configured() {
        String endpoint = System.getenv("AADHAAR_PROVIDER_ENDPOINT");
        String clientId = System.getenv("AADHAAR_PROVIDER_CLIENT_ID");
        String secret = System.getenv("AADHAAR_PROVIDER_CLIENT_SECRET");
        return endpoint != null && !endpoint.isBlank()
                && clientId != null && !clientId.isBlank()
                && secret != null && !secret.isBlank();
    }
    @Override public String requestOtp(String aadhaarNumber, String consentReference) {
        if (!configured()) throw new AadhaarProviderNotConfiguredException();
        throw new UnsupportedOperationException("Implement the provider-specific signed API adapter after provider onboarding.");
    }
    @Override public boolean verifyOtp(String transactionId, String otp) {
        if (!configured()) throw new AadhaarProviderNotConfiguredException();
        throw new UnsupportedOperationException("Implement the provider-specific OTP verification adapter after provider onboarding.");
    }
}
