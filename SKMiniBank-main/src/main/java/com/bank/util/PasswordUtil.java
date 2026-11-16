package com.bank.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {

    private static final int SALT_BYTES = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        String saltStr = Base64.getEncoder().encodeToString(salt);
        String hash = computeHash(password, saltStr);
        return saltStr + ":" + hash;
    }

    public static boolean verifyPassword(String password, String storedHash) {
        if (password == null || storedHash == null || !storedHash.contains(":")) {
            // Also check plaintext fallback for legacy admin if any, then return false
            if (storedHash != null && storedHash.equals(password)) {
                return true;
            }
            return false;
        }
        String[] parts = storedHash.split(":", 2);
        String saltStr = parts[0];
        String expectedHash = parts[1];
        String computed = computeHash(password, saltStr);
        return MessageDigest.isEqual(
            computed.getBytes(StandardCharsets.UTF_8),
            expectedHash.getBytes(StandardCharsets.UTF_8)
        );
    }

    private static String computeHash(String password, String saltStr) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(saltStr.getBytes(StandardCharsets.UTF_8));
            byte[] hashed = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}
