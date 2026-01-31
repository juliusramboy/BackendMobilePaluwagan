package com.example.MobilePaluwagan.Controller;

import com.example.MobilePaluwagan.DTOs.Request.ApplyLoanRequest;
import com.example.MobilePaluwagan.DTOs.Request.CalculateLoanRequest;
import com.example.MobilePaluwagan.DTOs.Request.LoanApplicationRequest;
import com.example.MobilePaluwagan.DTOs.Response.*;
import com.example.MobilePaluwagan.Entity.LoanApplication;
import com.example.MobilePaluwagan.Entity.UserPrinciple;
import com.example.MobilePaluwagan.Service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @GetMapping("/loan")
    public ResponseEntity<String> dashboard(Principal principal) {
        return ResponseEntity.ok("Welcome to the LoanPanel, " + principal.getName());
    }



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

    @GetMapping("/loan/status")
    public ApiResponse<LoanStatusResponse> checkLoanStatus(Authentication authentication) {
        UserPrinciple userDetails = (UserPrinciple) authentication.getPrincipal();
        Long userId = userDetails.userId();

        LoanStatusResponse status = loanService.getUserLoanStatus(userId);

        return new ApiResponse<>(true, "Success", status);
    }

    @GetMapping("loan/status/details")
    public Optional<LoanApplication> details(Authentication authentication){
        UserPrinciple userDetails = (UserPrinciple) authentication.getPrincipal();
        Long userId = userDetails.userId();

        Optional<LoanApplication> userInfo = loanService.getDetails(userId);
        
        return userInfo;
    }


}
