package com.example.MobilePaluwagan.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${app.logo.url}")
    private String logoUrl;

    @Async
    public void sendVerificationEmail(String email, String verificationToken){
        String subject = "Verify your email address";
        String msg = "Welcome to Mobile Savings Loan at Pitogo! Please enter the verification code below to complete your registration.";
        sendEmail(email, verificationToken, subject, null, msg);
    }
    @Async
    public void resendVerificationEmail(String email, String verificationToken){
        String subject = "Verify your email address";
        String msg = "Here's your verification code again. Please enter it to complete your registration.";
        sendEmail(email, verificationToken, subject, null, msg);
    }
    @Async
    public void sendOtpInLogin(String email, String verificationToken){
        String subject = "Your two-factor sign in code";
        String msg = "Hey there! We noticed a login attempt from a new device. To protect your funds, please enter the code below.";
        sendEmail(email, verificationToken, subject, null, msg);
    }
    @Async
    public void sendForgotPasswordEmail(String email, String resetToken){
        String subject = "Reset your password";
        String msg = "We received a request to reset your password. Please enter the verification code below to proceed.";
        sendEmail(email, resetToken ,subject, null, msg);
    }


    private void sendEmail(String email, String token, String subject, String path, String msg){
        try{
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            String content = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Verification Code</title>
        </head>
        <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background-color: #f8fafc;">
            <table role="presentation" style="width: 100%%; background-color: #f8fafc; padding: 48px 16px;">
                <tr>
                    <td align="center">
                        <table role="presentation" style="max-width: 600px; width: 100%%; background-color: #ffffff; border-radius: 16px; box-shadow: 0 8px 40px -12px rgba(14, 165, 233, 0.15); border: 1px solid #e0f2fe; overflow: hidden;">
                            
                            <tr>
                                <td style="height: 6px; background-color: #0ea5e9;"></td>
                            </tr>
                            
                            <tr>
                                <td style="padding: 32px 32px 24px 32px;">
                                    <table role="presentation" style="width: 100%%;">
                                        <tr>
                                            <td>
                                                <table role="presentation">
                                                    <tr>
                                                        <td style="background-color: #f0f9ff; padding: 8px; border-radius: 8px; vertical-align: middle;">
                                                            <img src="cid:logoImage" alt="Logo" style="width: 40px; height: 40px; display: block;" />
                                                        </td>
                                                        <td style="padding-left: 12px; vertical-align: middle;">
                                                            <div style="font-size: 14px; font-weight: 600; color: #0f172a; line-height: 1.2;">Savings and Loan at Pitogo</div>
                                                            <div style="font-size: 10px; color: #0ea5e9; font-weight: 500; text-transform: uppercase; letter-spacing: 0.5px; margin-top: 4px;">Secure Platform</div>
                                                        </td>
                                                    </tr>
                                                </table>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                            
                            <tr>
                                <td style="padding: 0 32px 32px 32px;">
                                    <h1 style="font-size: 20px; font-weight: 500; color: #0f172a; margin: 0 0 8px 0;">%s</h1>
                                    <p style="font-size: 14px; color: #64748b; line-height: 1.6; margin: 0 0 24px 0;">%s</p>
                                    
                                    <table role="presentation" style="width: 100%%; margin: 24px 0;">
                                        <tr>
                                            <td style="background: linear-gradient(135deg, #f0f9ff 0%%, #e0f2fe 100%%); border: 2px solid #bae6fd; border-radius: 12px; padding: 32px; text-align: center;">
                                                <div style="font-size: 10px; font-weight: 800; color: #3A9AFF; text-transform: uppercase; letter-spacing: 2px; margin-bottom: 12px;">Verification Code</div>
                                                <div style="font-family: 'Courier New', monospace; font-size: 32px; font-weight: 600; letter-spacing: 8px; color: #082f49; margin: 12px 0;">%s</div>
                                                <div style="background-color: rgba(255, 255, 255, 0.7); display: inline-block; padding: 6px 12px; border-radius: 6px; margin-top: 12px;">
                                                    <span style="font-size: 11px; color: #0c4a6e; font-weight: 600;"> Expires in 10 minutes</span>
                                                </div>
                                            </td>
                                        </tr>
                                    </table>
                                    
                                    <table role="presentation" style="width: 100%%; background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 16px; margin-top: 24px;">
                                        <tr>
                                            <td style="vertical-align: top; padding-right: 12px; color: #94a3b8; font-size: 16px;">ℹ️</td>
                                            <td style="vertical-align: top;">
                                                <p style="font-size: 12px; color: #262626; line-height: 1.6; margin: 0;">
                                                    If you didn't request this action, someone might be trying to access your account. Please contact support immediately.
                                                </p>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                            
                            <tr>
                                <td style="background-color: #f8fafc; border-top: 1px solid #e2e8f0; padding: 32px; text-align: center;">
                                    <table role="presentation" style="width: 100%%;">
                                        <tr>
                                            <td style="padding-bottom: 16px;">
                                                <a href="#" style="color: #64748b; text-decoration: none; font-size: 12px; font-weight: 500; margin: 0 12px;">Help Center</a>
                                                <a href="#" style="color: #64748b; text-decoration: none; font-size: 12px; font-weight: 500; margin: 0 12px;">Security Tips</a>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td style="padding-bottom: 16px;">
                                                <p style="font-size: 12px; color: #262626; line-height: 1.6; margin: 0;">
                                                    Sent by Savings and Loan at Pitogo (SLP)<br>
                                                    Dilang 2 Pitogo Street, Barangay Pinagbuhatan, Pasig City, 1600 Metro Manila, Philippines
                                                </p>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>
                                                <p style="font-size: 10px; color: #262626; text-transform: uppercase; margin: 0;">
                                                    © 2026 All Rights Reserved
                                                </p>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                            
                        </table>
                    </td>
                </tr>
            </table>
        </body>
        </html>
        """.formatted(subject, msg, formatToken(token));

            helper.setTo(email);
            helper.setSubject(subject);
            helper.setFrom(from);
            helper.setText(content, true);

            // Add inline image
            ClassPathResource logoResource = new ClassPathResource("static/images/Logo.png");
            helper.addInline("logoImage", logoResource);

            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            System.err.println("failed to send email: " + e.getMessage());
        }
    }

    private String formatToken(String token) {
        if (token == null || token.length() != 6) {
            return token;
        }
        return token.substring(0, 3) + " " + token.substring(3);
    }

}
