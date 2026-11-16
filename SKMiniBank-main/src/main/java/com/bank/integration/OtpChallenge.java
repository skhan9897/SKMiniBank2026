
package com.bank.integration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

public final class OtpChallenge {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long TTL = 5 * 60 * 1000L;
    private static final int MAX_ATTEMPTS = 5;

    private OtpChallenge() {
    }

    public static String generate() {
        return String.format("%06d", RANDOM.nextInt(1000000));
    }

    public static void store(
            HttpServletRequest request,
            String purpose,
            String destination,
            String otp) {

        HttpSession session = request.getSession();

        String key = "SKMB_OTP_" + purpose.toUpperCase();

        session.setAttribute(key + "_HASH", hash(otp));
        session.setAttribute(key + "_DESTINATION", destination);
        session.setAttribute(key + "_EXPIRY",
                System.currentTimeMillis() + TTL);
        session.setAttribute(key + "_LAST_SENT", System.currentTimeMillis());
        session.setAttribute(key + "_ATTEMPTS", 0);
        session.setAttribute(key + "_VERIFIED", false);
    }

    public static boolean isCooldownActive(HttpServletRequest request, String purpose, long cooldownMs) {
        if (request == null) return false;
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        String key = "SKMB_OTP_" + purpose.toUpperCase() + "_LAST_SENT";
        Object lastSent = session.getAttribute(key);
        if (lastSent instanceof Long) {
            long elapsed = System.currentTimeMillis() - (Long) lastSent;
            return elapsed < cooldownMs;
        }
        return false;
    }

    public static long getRemainingCooldownSeconds(HttpServletRequest request, String purpose, long cooldownMs) {
        if (request == null) return 0;
        HttpSession session = request.getSession(false);
        if (session == null) return 0;
        String key = "SKMB_OTP_" + purpose.toUpperCase() + "_LAST_SENT";
        Object lastSent = session.getAttribute(key);
        if (lastSent instanceof Long) {
            long elapsed = System.currentTimeMillis() - (Long) lastSent;
            if (elapsed < cooldownMs) {
                return (cooldownMs - elapsed) / 1000L;
            }
        }
        return 0;
    }

    public static boolean isVerified(HttpServletRequest request, String purpose) {
        if (request == null) return false;
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        String key = "SKMB_OTP_" + purpose.toUpperCase() + "_VERIFIED";
        Object verified = session.getAttribute(key);
        return verified instanceof Boolean && (Boolean) verified;
    }

    public static boolean verify(
            HttpServletRequest request,
            String purpose,
            String destination,
            String otp) {

        if (request == null || otp == null
                || !otp.matches("[0-9]{6}")) {
            return false;
        }

        HttpSession session = request.getSession(false);

        if (session == null) {
            return false;
        }

        String key = "SKMB_OTP_" + purpose.toUpperCase();

        Object savedHash = session.getAttribute(key + "_HASH");
        Object savedDestination =
                session.getAttribute(key + "_DESTINATION");
        Object expiry = session.getAttribute(key + "_EXPIRY");
        Object attempts = session.getAttribute(key + "_ATTEMPTS");

        if (!(savedHash instanceof String)
                || !(savedDestination instanceof String)
                || !(expiry instanceof Long)
                || !(attempts instanceof Integer)) {
            return false;
        }

        if (!destination.equals(savedDestination)) {
            return false;
        }

        if (System.currentTimeMillis() > (Long) expiry
                || (Integer) attempts >= MAX_ATTEMPTS) {
            clear(session, purpose);
            return false;
        }

        session.setAttribute(
                key + "_ATTEMPTS",
                (Integer) attempts + 1
        );

        boolean valid = MessageDigest.isEqual(
                ((String) savedHash).getBytes(StandardCharsets.UTF_8),
                hash(otp).getBytes(StandardCharsets.UTF_8)
        );

        if (valid) {
            session.removeAttribute(key + "_HASH");
            session.setAttribute(key + "_VERIFIED", true);
            session.setAttribute(
                    key + "_VERIFIED_DESTINATION",
                    destination
            );
        }

        return valid;
    }

    public static void clear(
            HttpSession session,
            String purpose) {

        if (session == null) {
            return;
        }

        String key = "SKMB_OTP_" + purpose.toUpperCase();

        session.removeAttribute(key + "_HASH");
        session.removeAttribute(key + "_DESTINATION");
        session.removeAttribute(key + "_EXPIRY");
        session.removeAttribute(key + "_ATTEMPTS");
        session.removeAttribute(key + "_VERIFIED");
        session.removeAttribute(key + "_VERIFIED_DESTINATION");
    }

    private static String hash(String value) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));

            StringBuilder result = new StringBuilder();

            for (byte b : bytes) {
                result.append(String.format("%02x", b & 0xff));
            }

            return result.toString();

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
