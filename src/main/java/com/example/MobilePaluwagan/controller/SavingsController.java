package com.example.MobilePaluwagan.controller;

import com.example.MobilePaluwagan.annotation.Idempotent;
import com.example.MobilePaluwagan.annotation.RequiresTransactionToken;
import com.example.MobilePaluwagan.dto.Request.PaymentFilterRequest;
import com.example.MobilePaluwagan.dto.Request.PaymongoRequest;
import com.example.MobilePaluwagan.dto.Request.UserCreateSavingsAccount;
import com.example.MobilePaluwagan.dto.Request.UserDepositSavingsRequest;
import com.example.MobilePaluwagan.dto.Response.*;
import com.example.MobilePaluwagan.entity.UserPrinciple;
import com.example.MobilePaluwagan.entity.UserSavings;
import com.example.MobilePaluwagan.repository.UserSavingsRepo;
import com.example.MobilePaluwagan.service.PayMongoService;
import com.example.MobilePaluwagan.service.SavingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/savings")
@RequiredArgsConstructor()
public class SavingsController {


    private final SavingsService savingsService;

    private final UserSavingsRepo userSavingsRepo;
    private final PayMongoService  payMongoService;


    @GetMapping("/savings")
    @RequiresTransactionToken
    public ResponseEntity<String> dashboard(Principal principal) {
        return ResponseEntity.ok("Welcome to the savingsPanel, " + principal.getName());
    }

    @PostMapping("/remit")
    @RequiresTransactionToken
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
    public ResponseEntity<ApiResponse<SavingsSummaryResponse>> history(Authentication authentication, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size) {
        UserPrinciple user = (UserPrinciple) authentication.getPrincipal();
        Long userId = user.userId();

        ApiResponse<SavingsSummaryResponse> history = savingsService.savingsAllData(userId,page, size);

        return ResponseEntity.ok(history);
    }

    @GetMapping("/payment/filter")
    public ResponseEntity<SavingsResponse> filterSavings(
            Authentication authentication,
            @RequestParam(required = false) String reference,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        UserPrinciple userDetails = (UserPrinciple) authentication.getPrincipal();
        Long userId = userDetails.userId();

        PaymentFilterRequest filterRequest = PaymentFilterRequest.builder()
                .userId(userId)
                .reference(reference)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        if (!filterRequest.hasFilters()) {
            SavingsResponse response = SavingsResponse.builder()
                    .success(false)
                    .message("Please provide at least one filter (reference, startDate, or endDate)")
                    .filters(null)
                    .savings(Collections.emptyList())
                    .build();

            return ResponseEntity.ok(response);
        }

        List<UserSavings> savings = savingsService.filterSavingsPayment(filterRequest);

        List<SavingsDepositHistory> depostList = savings.stream()
                .map(savingsService::convertToDto)
                .toList();


        SavingsResponse response = SavingsResponse.builder()
                .success(true)
                .message("Successfully retrieved filtered payments")
                .filters(filterRequest.hasFilters() ? filterRequest : null)
                .savings(depostList)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-link")
    @Idempotent
    public ResponseEntity<ApiResponse<?>> createPaymentLink(@RequestBody PaymongoRequest request, Authentication authentication) {
        try {
            UserPrinciple userDetails = (UserPrinciple) authentication.getPrincipal();
            Long userId = userDetails.userId();
            String checkUrl = payMongoService.createPaymentLink(userId, request);


            return ResponseEntity.ok(new ApiResponse<>(true, "Payment link created", checkUrl));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }



}