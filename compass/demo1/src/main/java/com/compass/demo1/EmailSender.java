package com.compass.demo1;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailSender {

    private static final String EMAIL_FROM = "compass.bilkent@gmail.com";
    private static final String APP_PASSWORD = "fjrgrjsoqmaprard"; // no spaces

    public static void sendVerificationEmail(String toEmail, String code) {
        String subject = "Verification Code";
        String body = "Your verification code: " + code;
        sendEmail(toEmail, subject, body);
    }

    public static void sendForgotPassword(String toEmail, String code) {
        String subject = "Password Reset Code";
        String body = "Your password reset code is: " + code;
        sendEmail(toEmail, subject, body);
    }

    private static void sendEmail(String toEmail, String subject, String body) {
        if (toEmail == null || toEmail.trim().isEmpty()) {
            System.err.println("Recipient email is empty!");
            return;
        }

        final String cleanedEmail = toEmail.trim();

    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                Session session = getEmailSession();
                session.setDebug(true);
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(EMAIL_FROM));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(cleanedEmail));
                message.setSubject(subject);
                message.setText(body);
                Transport.send(message);
                System.out.println("Email sent successfully to: " + cleanedEmail);
            } catch (MessagingException e) {
                System.out.println("Error sending email to: " + cleanedEmail);
                e.printStackTrace();
            }
        }
    });
    thread.start();
    }

    private static Session getEmailSession() {
        return Session.getInstance(getGmailProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_FROM, APP_PASSWORD);
            }
        });
    }

    private static Properties getGmailProperties() {
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        prop.put("mail.smtp.connectiontimeout", "10000");
        prop.put("mail.smtp.timeout", "10000");
        prop.put("mail.smtp.writetimeout", "10000");
        return prop;
    }
}