package com.electivenavigator.utils;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailService {
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    // Using credentials provided by the user
    private static final String SENDER_EMAIL = "anshumanbhardwajhotels@gmail.com";
    private static final String SENDER_PASSWORD = "vwns cokt zmzu ofgf";

    public static void sendOtpEmail(String recipientEmail, String otpCode) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        // Required to avoid some SSL handshake errors on specific JVMs, but generally starttls is enough.
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SENDER_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject("Your OTP Code for Elective Navigator");

        // Professional HTML Email Content
        String htmlContent = "<div style=\"font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.05);\">"
                + "<div style=\"background: linear-gradient(135deg, #2c3e50, #000); padding: 25px; text-align: center;\">"
                + "<h2 style=\"color: #ffffff; margin: 0; font-size: 24px; font-weight: 800; letter-spacing: 1px;\">Elective Navigator</h2>"
                + "</div>"
                + "<div style=\"padding: 30px; background-color: #ffffff;\">"
                + "<p style=\"color: #34495e; font-size: 16px; line-height: 1.6; margin-top: 0;\">Hello,</p>"
                + "<p style=\"color: #34495e; font-size: 16px; line-height: 1.6;\">You initiated a request to reset your password. Please use the following One-Time Password (OTP) to proceed. This code is valid for 10 minutes.</p>"
                + "<div style=\"text-align: center; margin: 30px 0;\">"
                + "<span style=\"display: inline-block; background-color: #f8f9fa; color: #4f46e5; font-size: 32px; font-weight: bold; padding: 15px 30px; border-radius: 8px; border: 2px dashed #4f46e5; letter-spacing: 5px;\">" + otpCode + "</span>"
                + "</div>"
                + "<p style=\"color: #7f8c8d; font-size: 14px; margin-bottom: 0;\">If you did not request a password reset, please ignore this email or contact support if you have concerns.</p>"
                + "</div>"
                + "<div style=\"background-color: #f8f9fa; padding: 15px; text-align: center; border-top: 1px solid #eeeeee;\">"
                + "<p style=\"color: #95a5a6; font-size: 12px; margin: 0;\">&copy; " + java.time.Year.now().getValue() + " Elective Navigator. All rights reserved.</p>"
                + "</div>"
                + "</div>";

        message.setContent(htmlContent, "text/html; charset=utf-8");

        Transport.send(message);
        System.out.println("OTP email successfully sent to " + recipientEmail);
    }
}
