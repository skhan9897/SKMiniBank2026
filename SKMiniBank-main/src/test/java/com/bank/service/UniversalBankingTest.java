package com.bank.service;

import org.junit.Test;
import static org.junit.Assert.*;

public class UniversalBankingTest {

    @Test
    public void testAccountVerificationValidation() {
        AccountVerificationService service = new AccountVerificationService();
        AccountVerificationService.VerificationResult result = service.verifyAccount("", "", "");
        assertFalse(result.isSuccess());
        assertEquals("FAILED", result.getStatus());
    }

    @Test
    public void testUpiVerificationValidation() {
        UpiVerificationService service = new UpiVerificationService();
        UpiVerificationService.UpiVerificationResult result = service.verifyUpiId("invalid-vpa");
        assertFalse(result.isSuccess());
        assertEquals("FAILED", result.getStatus());

        UpiVerificationService.UpiVerificationResult validResult = service.verifyUpiId("testuser@okaxis");
        assertTrue(validResult.isSuccess());
        assertEquals("VERIFIED", validResult.getStatus());
    }

    @Test
    public void testPaymentRoutingAmountValidation() {
        PaymentRoutingService service = new PaymentRoutingService();
        PaymentRoutingService.TransferResult result = service.transferFunds(1, "12345678", "SBI", "87654321", "Test Payee", -50.0, "KEY-1", "ACCOUNT");
        assertFalse(result.isSuccess());
        assertEquals("FAILED", result.getStatus());
    }
}
