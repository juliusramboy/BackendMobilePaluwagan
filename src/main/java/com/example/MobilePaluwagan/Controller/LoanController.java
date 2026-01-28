package com.example.MobilePaluwagan.Controller;

import com.example.MobilePaluwagan.DTOs.Request.ApplyLoanRequest;
import com.example.MobilePaluwagan.DTOs.Request.CalculateLoanRequest;
import com.example.MobilePaluwagan.DTOs.Request.LoanApplicationRequest;
import com.example.MobilePaluwagan.DTOs.Response.ApiResponse;
import com.example.MobilePaluwagan.DTOs.Response.ApplyLoanResponse;
import com.example.MobilePaluwagan.DTOs.Response.LoanApplicationResponse;
import com.example.MobilePaluwagan.DTOs.Response.UserAllLoansResponse;
import com.example.MobilePaluwagan.Entity.UserPrinciple;
import com.example.MobilePaluwagan.Service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @GetMapping("/loan")
    public ResponseEntity<String> dashboard(Principal principal) {
        return ResponseEntity.ok("Welcome to the LoanPanel, " + principal.getName());
    }




//    @PostMapping("/loan/apply")
//    public ResponseEntity<ApiResponse<UserApplyLoanResponse>> applyLoan(Authentication authentication, @RequestBody LoanApplicationRequest request) {
//        UserPrinciple userDetails = (UserPrinciple) authentication.getPrincipal();
//        Long userId = userDetails.userId();
//
//        ApiResponse<UserApplyLoanResponse> response = loanService.loanApplication(
//                userId,
//                BigDecimal.valueOf(request.getLoanAmount()),
//                request.getTermLength(),
//                request.getStartDate()
//        );
//
//        return ResponseEntity.ok(response);
//    }

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


}
