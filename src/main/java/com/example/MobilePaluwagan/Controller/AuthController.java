package com.example.MobilePaluwagan.Controller;

import com.example.MobilePaluwagan.DTOs.Request.*;
import com.example.MobilePaluwagan.DTOs.Response.LoginResponse;
import com.example.MobilePaluwagan.DTOs.Response.OtpResponse;
import com.example.MobilePaluwagan.DTOs.Response.RegisterResponse;
import com.example.MobilePaluwagan.Entity.User;
import com.example.MobilePaluwagan.Repository.UserRepo;
import com.example.MobilePaluwagan.Repository.VerificationRepo;
import com.example.MobilePaluwagan.Service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    @Autowired
    private RegisterService registerService;
    @Autowired
    private LoginService loginService;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private VerificationRepo verificationRepo;
    @Autowired
    private EmailService emailService;
    @Autowired
    private AuthOtpService authOtpService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {

        User existingUser = userRepo.findByEmail(request.getEmail());

        if (existingUser != null && !existingUser.isActive()) {
            String verificationToken = JWTService.generateToken(request.getEmail(), existingUser.getId(), existingUser.getRole().getRoleName());
            String otpCode = registerService.resendOtp(existingUser.getId());

            emailService.resendVerificationEmail(existingUser.getEmail(), otpCode);

            return new ResponseEntity<>(
                    new RegisterResponse(existingUser.getEmail(), existingUser.getId(), verificationToken, "User exists but not yet verified. Verification email resent."),
                    HttpStatus.OK
            );

        } else if (existingUser != null) {
            return new ResponseEntity<>(
                    new RegisterResponse(existingUser.getEmail(), existingUser.getId(), null, "User already registered and verified"),
                    HttpStatus.CONFLICT
            );

        } else {
            String verificationOtp = registerService.register(request);

            User newUser = userRepo.findByEmail(request.getEmail());

            emailService.sendVerificationEmail(request.getEmail(), verificationOtp);

            return ResponseEntity.ok(
                    new RegisterResponse(request.getEmail(), newUser.getId(), verificationOtp, "User registered successfully")
            );
        }
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        User existingUser = userRepo.findByEmail(request.getEmail());

        if(existingUser != null && !existingUser.isActive()){
            String verificationToken = JWTService.generateToken(request.getEmail(), existingUser.getId(), existingUser.getRole().getRoleName());
            emailService.sendVerificationEmail(request.getEmail(), verificationToken);
            return  ResponseEntity.ok( new LoginResponse(existingUser.getEmail(), existingUser.getId() , null, "User exists but not yet verified"));
        }
        return loginService.login(request);
    }

    @PostMapping("/otp/{userId}")
    public ResponseEntity<String> otp(@PathVariable Long userId, @RequestBody OtpRequest authOtp){
        authOtp.setUserId(userId);
        boolean isValid = authOtpService.otpFilter(authOtp);

        if(isValid){
            User user = userRepo.findById(userId).orElseThrow();
            String sessionToken = JWTService.generateToken(String.valueOf(user.getEmail()), user.getId(), user.getRole().getRoleName());
            return ResponseEntity.ok("OTP verified successfully " + sessionToken);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid or expired OTP");
        }
    }

    @PostMapping("/resend-email/{userId}")
    public ResponseEntity<?> resendMail(@PathVariable Long userId){

        boolean isResend = authOtpService.resendEmail(userId);

        if(isResend){
            return ResponseEntity.ok("Successfully re-send the OTP in your email");
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("failed to re-send the OTP in your email");
        }
    }

    @PostMapping("/login-send-otp")
    public ResponseEntity<OtpResponse> sendOtpLogin(@RequestBody OtpLoginRequest request) {

        OtpResponse response = authOtpService.sendOtpLogin(request.getEmail(), request.getPassword());
            if (response.getUserId() != null) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(response);
        }

    }

    @PostMapping("/forgot-password")
    public ResponseEntity<OtpResponse> forgotpassword(@RequestBody OtpLoginRequest request) {
        OtpResponse response = authOtpService.sendOtpLogin(request.getEmail(), request.getPassword());

        if (response.getUserId() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }
    }

    @PostMapping("/verify-forgotPass")
    public ResponseEntity<String> verifyOtpForgot(@RequestBody OtpVerifyForgotRequest request) {
        try {
            registerService.forgotPassword(request);
            return ResponseEntity.ok("Successfully changed your password");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while changing the password");
        }
    }


}
