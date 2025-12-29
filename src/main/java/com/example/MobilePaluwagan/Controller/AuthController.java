package com.example.MobilePaluwagan.Controller;

import com.example.MobilePaluwagan.DTOs.Request.LoginRequest;
import com.example.MobilePaluwagan.DTOs.Request.RegisterRequest;
import com.example.MobilePaluwagan.DTOs.Response.LoginResponse;
import com.example.MobilePaluwagan.Entity.User;
import com.example.MobilePaluwagan.Service.LoginService;
import com.example.MobilePaluwagan.Service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    @Autowired
    private RegisterService registerService;
    @Autowired
    private LoginService loginService;


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        registerService.register(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return loginService.login(request);
    }
}
