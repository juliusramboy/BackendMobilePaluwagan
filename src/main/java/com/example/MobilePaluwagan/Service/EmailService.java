package com.example.MobilePaluwagan.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;


    public void sendVerificationEmail(String email, String verificationToken){
        String subject = "Email Verification";
        String msg = "Click the button below to verify your email address";
        sendEmail(email, verificationToken, subject, null, msg);
    }

    public void sendForgotPasswordEmail(String email, String resetToken){
        String subject = "Password Reset Request";
        String msg = "Click the button below to reset your password";
        sendEmail(email, resetToken ,subject, null, msg);
    }

    private void sendEmail(String email, String token, String subject, String path, String msg){
        try{

            String content = "<!DOCTYPE html>" + "<html>" + "<head>" + "<style>" + "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color:#f4f4f4; margin:0; padding:0; }" + ".container { background-color:#ffffff; max-width:600px; margin:40px auto; padding:30px; border:1px solid #ddd; box-shadow:0 2px 6px rgba(0,0,0,0.1); }" + "h2 { color:#2c3e50; margin-bottom:20px; }" + "p { color:#333333; line-height:1.6; }" + ".otp { display:inline-block; background-color:#007BFF; color:#ffffff; padding:12px 24px; border-radius:4px; font-weight:bold; font-size:20px; letter-spacing:4px; }" + ".footer { margin-top:30px; font-size:12px; color:#777777; border-top:1px solid #eee; padding-top:15px; }" + "</style>" + "</head>" + "<body>" + "<div class='container'>" + "<h2>Mobile Paluwagan</h2>" + "<p>Dear User,</p>" + "<p>" + msg + "</p>" + "<p style='text-align:center;'>Your verification code:</p>" + "<p style='text-align:center;'><span class='otp'>" + token + "</span></p>" + "<p>If you did not request this action, please ignore this email.</p>" + "<div class='footer'>" + "<p>Best regards,<br>Mobile Paluwagan Support Team</p>" + "<p>This is an automated message. Please do not reply directly to this email.</p>" + "</div>" + "</div>" + "</body>" + "</html>";

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo(email);
            helper.setSubject(subject);
            helper.setFrom(from);
            helper.setText(content, true);
            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            System.err.println("failed to send email: " + e.getMessage());
        }
    }

}
