package com.bank.integration.pan;

public interface PanVerificationProvider {

    class PanResult {
        private final boolean success;
        private final String status;
        private final String referenceId;
        private final String message;

        public PanResult(boolean success, String status, String referenceId, String message) {
            this.success = success;
            this.status = status;
            this.referenceId = referenceId;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String getStatus() { return status; }
        public String getReferenceId() { return referenceId; }
        public String getMessage() { return message; }
    }

    PanResult verifyPan(String panNumber, String fullName, String dob);
}
