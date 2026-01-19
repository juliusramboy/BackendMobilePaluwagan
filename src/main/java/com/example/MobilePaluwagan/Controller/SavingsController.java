package com.example.MobilePaluwagan.Controller;

import com.example.MobilePaluwagan.DTOs.Request.UserDepositSavingsRequest;
import com.example.MobilePaluwagan.DTOs.Response.ApiResponse;
import com.example.MobilePaluwagan.DTOs.Response.SavingsDepositHistory;
import com.example.MobilePaluwagan.DTOs.Response.SavingsSummaryResponse;
import com.example.MobilePaluwagan.DTOs.Response.UserDepositSavingsResponse;
import com.example.MobilePaluwagan.Entity.UserPrinciple;
import com.example.MobilePaluwagan.Service.SavingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;

@RestController
@RequestMapping("/api/savings")
public class SavingsController {

    @Autowired
    private SavingsService savingsService;

    @GetMapping("/savings")
    public ResponseEntity<String> dashboard(Principal principal) {
        return ResponseEntity.ok("Welcome to the savingsPanel, " + principal.getName());
    }

    @PostMapping("/remit")
    public ResponseEntity<ApiResponse<UserDepositSavingsResponse>> userDepositSavings(Authentication authentication, @RequestBody UserDepositSavingsRequest request){
        UserPrinciple user =  (UserPrinciple) authentication.getPrincipal();
        Long userId = user.userId();

        ApiResponse<UserDepositSavingsResponse> savings = savingsService.userDeposit(
                userId,
                request.getAmountDeposit(),
                request.getDepositDate()
        );

        return ResponseEntity.ok(savings);
    }



    @GetMapping("summary")
    public ResponseEntity<ApiResponse<SavingsSummaryResponse>> history(Authentication authentication){
        UserPrinciple user = (UserPrinciple) authentication.getPrincipal();
        Long userId = user.userId();

        ApiResponse<SavingsSummaryResponse> history = savingsService.savingsAllData(userId);

        return ResponseEntity.ok(history);
    }
}