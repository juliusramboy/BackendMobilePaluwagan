package com.example.MobilePaluwagan.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api")
public class SavingsController {

    @GetMapping("/savings")
    public ResponseEntity<String> dashboard(Principal principal) {
        return ResponseEntity.ok("Welcome to the savingsPanel, " + principal.getName());
    }
}