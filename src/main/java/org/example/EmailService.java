package org.example;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailService {

    private final String username = "textbooksocials@gmail.com"; // Replace with your Gmail email address
    private final String password = "fhuyfjwfwhzvnlss"; // Replace with your Gmail password
    private final Properties properties;

    public EmailService() {
        properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587"); // Port for TLS
        properties.put("mail.smtp.starttls.enable", "true"); // Enable STARTTLS
    }

    public void sendPasswordResetEmail(String recipientEmail, String resetCode) {
        Session session = Session.getInstance(properties, new jakarta.mail.Authenticator() {
            protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                return new jakarta.mail.PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username)); // Use the sender email configured for the session
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Password Reset");
            message.setText("Your password reset code is: " + resetCode +
                    "\n\n\n\n\n\n If you did not request this, please ignore this email.");

            Transport.send(message);

            System.out.println("Password reset email sent successfully!");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
