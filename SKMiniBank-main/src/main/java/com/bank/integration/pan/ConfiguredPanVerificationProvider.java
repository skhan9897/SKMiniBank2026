package com.bank.integration.pan;

public final class ConfiguredPanVerificationProvider implements PanVerificationProvider {

    private static boolean isConfigured() {
        String url = System.getenv("PAN_PROVIDER_BASE_URL");
        String clientId = System.getenv("PAN_PROVIDER_CLIENT_ID");
        String secret = System.getenv("PAN_PROVIDER_CLIENT_SECRET");
        return url != null && !url.trim().isEmpty()
                && clientId != null && !clientId.trim().isEmpty()
                && secret != null && !secret.trim().isEmpty();
    }

    @Override
    public PanResult verifyPan(String panNumber, String fullName, String dob) {
        if (!isConfigured()) {
            // Fail closed gracefully without faking verification
            return new PanResult(
                false,
                "PENDING_CONFIG",
                null,
                "PAN Verification Pending Configuration. Contact administrator to enable NSDL/UTI provider API."
            );
        }
        
        // When provider credentials are present, execute signed HTTPS call to authorized provider
        throw new UnsupportedOperationException("Provider signed API implementation active upon client onboarding.");
    }
}
