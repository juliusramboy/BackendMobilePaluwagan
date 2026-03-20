package com.example.MobilePaluwagan.controller;

import com.example.MobilePaluwagan.dto.Request.ChatRequest;
import com.example.MobilePaluwagan.service.CustomerServiceAIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class CustomerServiceController {

    private final CustomerServiceAIService customerServiceAIService;

    @PostMapping
    public ResponseEntity<?> chat(
            @RequestBody ChatRequest request,
            Authentication authentication) {
        try {
            String response = customerServiceAIService
                    .chat(request.getMessage());
            return ResponseEntity.ok(Map.of("response", response));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
