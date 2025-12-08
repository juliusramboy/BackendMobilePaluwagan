package com.example.MobilePaluwagan.Controller;

import com.example.MobilePaluwagan.DTOs.Request.RegisterRequest;
import com.example.MobilePaluwagan.Service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/register")
public class UserRegisterController {

    @Autowired
    private RegisterService registerService;


    @PostMapping
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        registerService.register(request);
        return ResponseEntity.ok("User registered successfully");
    }
}
