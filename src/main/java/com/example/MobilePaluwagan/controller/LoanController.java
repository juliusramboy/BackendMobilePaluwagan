package com.example.MobilePaluwagan.controller;

import com.example.MobilePaluwagan.dto.Request.ApplyLoanRequest;
import com.example.MobilePaluwagan.dto.Request.CalculateLoanRequest;
import com.example.MobilePaluwagan.dto.Request.PaymentFilterRequest;
import com.example.MobilePaluwagan.dto.Response.*;
import com.example.MobilePaluwagan.entity.LoanApplication;
import com.example.MobilePaluwagan.entity.LoanPayment;
import com.example.MobilePaluwagan.entity.UserPrinciple;
import com.example.MobilePaluwagan.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api")
public class  LoanController {

    @Autowired
    private LoanService loanService;


    @GetMapping("/loan/user-details")
    public ResponseEntity<ApiResponse<UserAllLoansResponse>> getLoanInfoFromUser(Authentication authentication){
        UserPrinciple loanInfo = (UserPrinciple) authentication.getPrincipal();
        Long userId = loanInfo.userId();

        ApiResponse<UserAllLoansResponse> userInfo = loanService.getAllTheInfo(userId);

        return ResponseEntity.ok(userInfo);
    }


    @PostMapping("/loan/calculate-loan")
    public ResponseEntity<ApplyLoanResponse> calculateLoan(Authentication authentication, @RequestBody CalculateLoanRequest request){
        UserPrinciple userDetails = (UserPrinciple) authentication.getPrincipal();
        Long userId = userDetails.userId();

        ApplyLoanResponse response = loanService.processLoanApplication(
                userId,
                request.getLoanAmount(),
                request.getStartDate(),
                request.getEndDate()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/loan/apply-loan")
    public Long applyLoan(Authentication authentication, @RequestBody ApplyLoanRequest request){
        UserPrinciple userDetails = (UserPrinciple) authentication.getPrincipal();
        Long userId = userDetails.userId();

        Long response = loanService.applyLoan(userId, request);

        return response;
    }


    @GetMapping("/loan/status/details")
    public Optional<LoanApplication> details(Authentication authentication){
        UserPrinciple userDetails = (UserPrinciple) authentication.getPrincipal();
        Long userId = userDetails.userId();

        Optional<LoanApplication> userInfo = loanService.getDetails(userId);
        
        return userInfo;
    }

    @GetMapping("/loan/payment/filter")
    public ResponseEntity<paymentResponse> filterPayment(
            Authentication authentication,
            @RequestParam(required = false) String reference,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {

        // Get the logged-in user's ID
        UserPrinciple userDetails = (UserPrinciple) authentication.getPrincipal();
        Long userId = userDetails.userId();  // Make sure your UserPrinciple has getUserId() method

        // Step 1: Build the filter object with userId
        PaymentFilterRequest filter = PaymentFilterRequest.builder()
                .userId(userId)
                .reference(reference)
                .startDate(startDate)
                .endDate(endDate)
                .status(status)
                .paymentMethod(paymentMethod)
                .build();

        // Step 2: Get filtered payments from service
        Page<LoanPayment> loanPayments = loanService.filterUserPayments(filter, page, size);

        // Step 3: Convert entities to DTOs
        List<PaymentInfo> paymentDTOs = loanPayments.getContent() // ← getContent()
                .stream()
                .map(loanService::convertToDTO)
                .collect(Collectors.toList());

        // Step 4: Create appropriate message
        String message = filter.hasFilters() ?
                "Successfully retrieved filtered payments" :
                "Successfully retrieved all payments";

        // Step 5: Build the response
        paymentResponse response = paymentResponse.builder()
                .success(true)
                .message(message)
                .filters(filter.hasFilters() ? filter : null)
                .payment(paymentDTOs)
                .currentPage(loanPayments.getNumber())
                .totalPages(loanPayments.getTotalPages())
                .totalElements(loanPayments.getTotalElements())
                .last(loanPayments.isLast())
                .build();

        return ResponseEntity.ok(response);
    }


}
