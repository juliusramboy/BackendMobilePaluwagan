package com.example.MobilePaluwagan.Controller;

import com.example.MobilePaluwagan.DTOs.Request.LoginRequest;
import com.example.MobilePaluwagan.DTOs.Request.RegisterRequest;
import com.example.MobilePaluwagan.DTOs.Request.VerificationRequest;
import com.example.MobilePaluwagan.DTOs.Response.LoginResponse;
import com.example.MobilePaluwagan.Entity.User;
import com.example.MobilePaluwagan.Repository.UserRepo;
import com.example.MobilePaluwagan.Repository.VerificationRepo;
import com.example.MobilePaluwagan.Service.LoginService;
import com.example.MobilePaluwagan.Service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

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


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {

        User existingUser = userRepo.findByEmail(request.getEmail());

        if(existingUser != null && !existingUser.isActive()){
            return new ResponseEntity<>("User exists but not yet verified", HttpStatus.BAD_REQUEST);
        } else if (existingUser != null && existingUser.isActive()) {
            return new ResponseEntity<>("User already signup and verified email", HttpStatus.BAD_REQUEST);
        } else {
            registerService.register(request);
            return ResponseEntity.ok("User registered successfully");
        }

    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return loginService.login(request);
    }
}
