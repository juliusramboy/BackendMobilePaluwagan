package com.example.MobilePaluwagan.Controller;

import com.example.MobilePaluwagan.DTOs.Request.LoginRequest;
import com.example.MobilePaluwagan.DTOs.Request.OtpRequest;
import com.example.MobilePaluwagan.DTOs.Request.RegisterRequest;
import com.example.MobilePaluwagan.DTOs.Response.LoginResponse;
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


        if(existingUser != null && !existingUser.isActive()){
            String verificationToken = JWTService.generateToken(request.getEmail());
            emailService.sendVerificationEmail(existingUser.getEmail(), verificationToken);
            return new ResponseEntity<>( new RegisterResponse(existingUser.getEmail(), existingUser.getId(), verificationToken,"User exists but not yet verified"), HttpStatus.BAD_REQUEST );

        } else if (existingUser != null) {
            String verificationToken = registerService.register(request);
            return new ResponseEntity<>( new RegisterResponse(existingUser.getEmail(),  existingUser.getId(),verificationToken,"User already signup and verified email"), HttpStatus.CONFLICT);
        } else {
            String verificationToken = registerService.register(request);
            User newUser = userRepo.findByEmail(request.getEmail());
            emailService.sendVerificationEmail(request.getEmail(), verificationToken);
            return ResponseEntity.ok( new RegisterResponse(request.getEmail(), newUser.getId(),verificationToken,"User registered successfully: ") );
        }

    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        User existingUser = userRepo.findByEmail(request.getEmail());
        if(existingUser != null && !existingUser.isActive()){
            String verificationToken = JWTService.generateToken(request.getEmail());
            emailService.sendVerificationEmail(request.getEmail(), verificationToken);
            return new ResponseEntity<>( new RegisterResponse(existingUser.getEmail(), existingUser.getId(), verificationToken,"User exists but not yet verified"), HttpStatus.BAD_REQUEST );
        }
        return loginService.login(request);
    }

    @PostMapping("/otp/{userId}")
    public ResponseEntity<String> otp(@PathVariable Long userId, @RequestBody OtpRequest authOtp){
        authOtp.setUserId(userId);
        boolean isValid = authOtpService.otpFilter(authOtp);

        if(isValid){
            User user = userRepo.findById(userId).orElseThrow();
            String sessionToken = JWTService.generateToken(String.valueOf(user.getEmail()));
            return ResponseEntity.ok("OTP verified successfully " + sessionToken);

        }else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid or expired OTP");
        }

    }
}
