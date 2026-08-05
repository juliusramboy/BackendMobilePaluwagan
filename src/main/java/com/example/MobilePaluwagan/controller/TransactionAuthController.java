package com.example.MobilePaluwagan.controller;

import com.example.MobilePaluwagan.annotation.PaymentRateLimited;
import com.example.MobilePaluwagan.dto.Request.VerifyPinRequest;
import com.example.MobilePaluwagan.entity.UserPrinciple;
import com.example.MobilePaluwagan.service.TransactionAuthService;
import com.example.MobilePaluwagan.service.TransactionTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionAuthController {

    private final TransactionAuthService transactionAuthService;
    private final TransactionTokenService transactionTokenService;

    @PostMapping("/verify-pin")
    @PaymentRateLimited(action = "verify-pin", maxAttempts = 5, windowSeconds = 900)
    public ResponseEntity<?> verifyPin(@Valid @RequestBody VerifyPinRequest request, Authentication authentication){
        UserPrinciple user = (UserPrinciple) authentication.getPrincipal();
        Long userId = user.userId();

        transactionAuthService.verifyPin(userId, request.getTransactionPin());

        String token = transactionTokenService.issueToken(String.valueOf(userId));

        return ResponseEntity.ok(Map.of(
                "transactionToken", token,
                "expiresIn", 120
        ));
    }
}
