package com.example.MobilePaluwagan.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String from;

    @Value("${app.logo.url:}")
    private String logoUrl;

    @Async
    public void sendVerificationEmail(String email, String verificationToken){
        String subject = "Verify your email address";
        String msg = "Here is your verification code:";
        sendEmail(email, verificationToken, subject, null, msg);
    }

    @Async
    public void resendVerificationEmail(String email, String verificationToken){
        String subject = "Verify your email address";
        String msg = "Here is your verification code again:";
        sendEmail(email, verificationToken, subject, null, msg);
    }

    @Async
    public void sendOtpInLogin(String email, String verificationToken){
        String subject = "Your two-factor sign in code";
        String msg = "Here is your authentication code:";
        sendEmail(email, verificationToken, subject, null, msg);
    }

    @Async
    public void sendForgotPasswordEmail(String email, String resetToken){
        String subject = "Reset your password";
        String msg = "Here is your password reset code:";
        sendEmail(email, resetToken, subject, null, msg);
    }

    private void sendEmail(String email, String token, String subject, String path, String msg){
        try{
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            String username = (email != null && email.contains("@")) ? email.substring(0, email.indexOf("@")) : "user";
            String logoHeader = (logoUrl != null && !logoUrl.isBlank()) 
                ? "<img src=\"" + logoUrl + "\" alt=\"Logo\" width=\"48\" height=\"48\" style=\"display: block; margin: 0 auto 16px auto; border-radius: 50%;\" />"
                : """
                  <svg height="48" viewBox="0 0 16 16" width="48" style="display: block; margin: 0 auto 16px auto; fill: #24292f;">
                      <path d="M8 0c4.42 0 8 3.58 8 8a8.013 8.013 0 0 1-5.45 7.59c-.4.08-.55-.17-.55-.38 0-.27.01-1.13.01-2.2 0-.75-.25-1.23-.54-1.48 1.78-.2 3.65-.88 3.65-3.95 0-.88-.31-1.59-.82-2.15.08-.2.36-1.02-.08-2.12 0 0-.67-.22-2.2.82-.64-.18-1.32-.27-2-.27-.68 0-1.36.09-2 .27-1.53-1.03-2.2-.82-2.2-.82-.44 1.1-.16 1.92-.08 2.12-.51.56-.82 1.28-.82 2.15 0 3.06 1.86 3.75 3.64 3.95-.23.2-.44.55-.51 1.07-.46.21-1.61.55-2.33-.66-.15-.24-.6-.83-1.23-.82-.67.01-.27.38.01.53.34.19.73.9.82 1.13.16.45.68 1.31 2.69.94 0 .67.01 1.3.01 1.49 0 .21-.15.45-.55.38A7.995 7.995 0 0 1 0 8c0-4.42 3.58-8 8-8Z"></path>
                  </svg>
                  """;

            String content = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>%s</title>
        </head>
        <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #ffffff; color: #24292e;">
            <table role="presentation" style="width: 100%%; background-color: #ffffff; padding: 40px 16px;">
                <tr>
                    <td align="center">
                        <table role="presentation" style="max-width: 540px; width: 100%%; text-align: left;">
                            
                            <!-- HEADER BRANDING -->
                            <tr>
                                <td align="center" style="padding-bottom: 16px;">
                                    %s
                                    <h1 style="font-size: 20px; font-weight: 600; color: #24292e; margin: 0; text-align: center; letter-spacing: -0.2px;">Please verify your identity, <strong>%s</strong></h1>
                                </td>
                            </tr>

                            <!-- MAIN CARD -->
                            <tr>
                                <td>
                                    <table role="presentation" style="width: 100%%; background-color: #ffffff; border-radius: 6px; border: 1px solid #d0d7de; padding: 24px 32px;">
                                        <tr>
                                            <td>
                                                <p style="font-size: 14px; color: #24292e; line-height: 1.5; margin: 0 0 16px 0;">%s</p>
                                                
                                                <!-- OTP CODE DISPLAY -->
                                                <div style="font-family: ui-monospace, SFMono-Regular, 'SF Mono', Menlo, Consolas, 'Liberation Mono', monospace; font-size: 28px; font-weight: 500; letter-spacing: 6px; color: #24292e; text-align: center; margin: 24px 0;">
                                                    %s
                                                </div>

                                                <p style="font-size: 14px; color: #24292e; line-height: 1.5; margin: 0 0 16px 0;">
                                                    This code is valid for <strong>15 minutes</strong> and can only be used once.
                                                </p>

                                                <p style="font-size: 14px; color: #24292e; line-height: 1.5; margin: 0 0 24px 0;">
                                                    <strong>Please don't share this code with anyone:</strong> we'll never ask for it on the phone or via email.
                                                </p>

                                                <p style="font-size: 14px; color: #24292e; line-height: 1.5; margin: 0;">
                                                    Thanks,<br>
                                                    <strong>The Savings Loan Team</strong>
                                                </p>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>

                            <!-- FOOTER DISCLAIMER & ADDRESS -->
                            <tr>
                                <td style="padding-top: 20px;">
                                    <p style="font-size: 12px; color: #57606a; line-height: 1.5; margin: 0 0 16px 0;">
                                        You're receiving this email because a verification code was requested for your account. If this wasn't you, please ignore this email.
                                    </p>
                                    <hr style="border: none; border-top: 1px solid #d0d7de; margin: 0 0 16px 0;" />
                                    <p style="font-size: 12px; color: #6e7781; line-height: 1.5; margin: 0; text-align: center;">
                                       Savings and Loan at Pitogo · Barangay Pinagbuhatan, Pasig City, 1602 Metro Manila, Philippines
                                    </p>
                                </td>
                            </tr>

                        </table>
                    </td>
                </tr>
            </table>
        </body>
        </html>
        """.formatted(subject, logoHeader, username, msg, formatToken(token));

            helper.setTo(email);
            helper.setSubject(subject);
            helper.setFrom(from);
            helper.setText(content, true);

            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            System.err.println("failed to send email: " + e.getMessage());
        }
    }

    private String formatToken(String token) {
        if (token == null) {
            return "";
        }
        return token.replaceAll(".(?=.)", "$0 ");
    }
}

