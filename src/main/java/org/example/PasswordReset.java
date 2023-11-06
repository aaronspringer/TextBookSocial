package org.example;

import java.security.SecureRandom;
import java.util.Base64;

public class PasswordReset {
    private int attempts = 0;
    private static final int MAX_ATTEMPTS = 3;
    private String hashedResetCode;
    private long resetCodeTimestamp;
    private EmailService emailService;

    public PasswordReset() {
        this.emailService = new EmailService();
    }

    public void recordFailedLoginAttempt() {
        attempts++;
    }

    public boolean shouldOfferReset() {
        return attempts >= MAX_ATTEMPTS;
    }

    public void initiatePasswordReset(String recipientEmail) {

        byte[] randomBytes = new byte[24];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(randomBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        hashedResetCode = SecurityUtils.hashPassword(token);

        resetCodeTimestamp = System.currentTimeMillis();

        emailService.sendPasswordResetEmail(recipientEmail, token);
    }

    public boolean verifyResetCode(String inputCode) {


        if (SecurityUtils.checkPassword(inputCode, hashedResetCode)) {
            long currentTime = System.currentTimeMillis();
            if ((currentTime - resetCodeTimestamp) <= 2 * 60 * 1000) {
                attempts = 0;
                return true;
            }
        }
        return false;
    }
}
