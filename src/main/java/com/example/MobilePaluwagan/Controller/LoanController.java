package com.example.MobilePaluwagan.Controller;

import com.example.MobilePaluwagan.DTOs.Request.LoanApplicationRequest;
import com.example.MobilePaluwagan.DTOs.Response.ApiResponse;
import com.example.MobilePaluwagan.DTOs.Response.UserAllLoansResponse;
import com.example.MobilePaluwagan.DTOs.Response.UserApplyLoanResponse;
import com.example.MobilePaluwagan.Entity.UserPrinciple;
import com.example.MobilePaluwagan.Service.LoanService;
import com.example.MobilePaluwagan.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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




    @PostMapping("/loan/apply")
    public ResponseEntity<UserApplyLoanResponse> applyLoan(Authentication authentication, @RequestBody LoanApplicationRequest request) {
        UserPrinciple userDetails = (UserPrinciple) authentication.getPrincipal();
        Long userId = userDetails.userId();

        UserApplyLoanResponse response = loanService.loanApplication(
                userId,
                BigDecimal.valueOf(request.getLoanAmount()),
                request.getTermLength()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/loan/user-details")
    public ResponseEntity<ApiResponse<UserAllLoansResponse>> getLoanInfoFromUser(Authentication authentication){
        UserPrinciple loanInfo = (UserPrinciple) authentication.getPrincipal();
        Long userId = loanInfo.userId();

        ApiResponse<UserAllLoansResponse> userInfo = loanService.getAllTheInfo(userId);

        return ResponseEntity.ok(userInfo);
    }




}
