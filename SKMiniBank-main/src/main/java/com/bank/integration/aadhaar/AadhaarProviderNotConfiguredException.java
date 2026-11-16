package com.bank.integration.aadhaar;

public class AadhaarProviderNotConfiguredException extends IllegalStateException {
    public AadhaarProviderNotConfiguredException() {
        super("Aadhaar OTP is unavailable: configure an authorized UIDAI-compliant authentication provider first.");
    }
}
