package com.example.MobilePaluwagan.Controller;

import com.example.MobilePaluwagan.DTOs.Request.PaymentFilterRequest;
import com.example.MobilePaluwagan.DTOs.Request.UserCreateSavingsAccount;
import com.example.MobilePaluwagan.DTOs.Request.UserDepositSavingsRequest;
import com.example.MobilePaluwagan.DTOs.Response.*;
import com.example.MobilePaluwagan.Entity.UserPrinciple;
import com.example.MobilePaluwagan.Entity.UserSavings;
import com.example.MobilePaluwagan.Repository.UserSavingsRepo;
import com.example.MobilePaluwagan.Service.SavingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/savings")
public class SavingsController {

    @Autowired
    private SavingsService savingsService;
    @Autowired
    private UserSavingsRepo userSavingsRepo;

    @GetMapping("/savings")
    public ResponseEntity<String> dashboard(Principal principal) {
        return ResponseEntity.ok("Welcome to the savingsPanel, " + principal.getName());
    }

    @PostMapping("/remit")
    public ResponseEntity<ApiResponse<UserDepositSavingsResponse>> userDepositSavings(Authentication authentication, @RequestBody UserDepositSavingsRequest request) {
        UserPrinciple user = (UserPrinciple) authentication.getPrincipal();
        Long userId = user.userId();

        ApiResponse<UserDepositSavingsResponse> savings = savingsService.userDeposit(
                userId,
                request.getAmountDeposit(),
                request.getDepositDate()
        );

        return ResponseEntity.ok(savings);
    }

    @PostMapping("/apply")
    public ResponseEntity<?> createSavingsAcc(Authentication authentication, @RequestBody UserCreateSavingsAccount request) {
        UserPrinciple user = (UserPrinciple) authentication.getPrincipal();
        Long userId = user.userId();

        ApiResponse<?> apply = savingsService.createSavingsAcc(
                userId,
                request.getTargetAmount(),
                request.getSourceOfFunds()
        );

        return ResponseEntity.ok(apply);
    }

    @PostMapping("/withdraw")
    public ApiResponse<?> UserWithdraw(Authentication authentication) {
        UserPrinciple user = (UserPrinciple) authentication.getPrincipal();
        Long userId = user.userId();

        return savingsService.withdrawSavings(userId);
    }


    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<SavingsSummaryResponse>> history(Authentication authentication) {
        UserPrinciple user = (UserPrinciple) authentication.getPrincipal();
        Long userId = user.userId();

        ApiResponse<SavingsSummaryResponse> history = savingsService.savingsAllData(userId);

        return ResponseEntity.ok(history);
    }

    @GetMapping("/payment/filter")
    public ResponseEntity<SavingsResponse> filterSavings(
            Authentication authentication,
            @RequestParam(required = false) String reference,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate endDate) {

        UserPrinciple userDetails = (UserPrinciple) authentication.getPrincipal();
        Long userId = userDetails.userId();

        PaymentFilterRequest filterRequest = PaymentFilterRequest.builder()
                .userId(userId)
                .reference(reference)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        List<UserSavings> savings = savingsService.filterSavingsPayment(filterRequest);

        List<SavingsDepositHistory> depostList = savings.stream()
                .map(savingsService::convertToDto)
                .toList();

        String msg = filterRequest.hasFilters() ?
                "Successfully retrieved filtered payments" :
                "Successfully retrieved all payments";

        SavingsResponse response = SavingsResponse.builder()
                .success(true)
                .message(msg)
                .filters(filterRequest.hasFilters() ? filterRequest : null)
                .savings(depostList)
                .build();

        return ResponseEntity.ok(response);
    }



}