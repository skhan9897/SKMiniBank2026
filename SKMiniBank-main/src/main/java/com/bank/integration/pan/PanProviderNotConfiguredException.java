package com.bank.integration.pan;

public class PanProviderNotConfiguredException extends RuntimeException {
    public PanProviderNotConfiguredException() {
        super("PAN Verification Pending Configuration: Authorized PAN Verification Provider API credentials are not set.");
    }
}
