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

    public void resetAttempts(){
        attempts = 0;
    }

    public void recordFailedLoginAttempt() {
        attempts++;
    }

    public boolean shouldOfferReset() {
        return attempts >= MAX_ATTEMPTS;
    }

    private static final String ALPHANUMERIC_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom secureRandom = new SecureRandom();

    private String generateAlphanumericCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int randomIndex = secureRandom.nextInt(ALPHANUMERIC_CHARS.length());
            code.append(ALPHANUMERIC_CHARS.charAt(randomIndex));
        }
        return code.toString();
    }

    public void initiatePasswordReset(String recipientEmail) {
        String token = generateAlphanumericCode();

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
