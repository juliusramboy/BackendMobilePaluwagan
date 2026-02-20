package com.example.MobilePaluwagan.Controller;

import com.example.MobilePaluwagan.DTOs.Request.UserDepositRequest;
import com.example.MobilePaluwagan.DTOs.Response.ApiResponse;
import com.example.MobilePaluwagan.DTOs.Response.LoanApplicantsAdmin;
import com.example.MobilePaluwagan.DTOs.Response.SavingsApplicantsAdmin;
import com.example.MobilePaluwagan.Service.LoanService;
import com.example.MobilePaluwagan.Service.SavingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/admin/savings")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSavingsController {

    @Autowired
    private SavingsService savingsService;

//    @PostMapping("/deposit")
//    public ResponseEntity<ApiResponse<?>> deposit(@RequestBody UserDepositRequest request){
//        return;
//    }


    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> getAllPendingApplicants() {
        ApiResponse<?>pending = savingsService.getSavingsPendingApplicants();
        return ResponseEntity.ok(pending);
    }

    @GetMapping("/paid")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> getAllApproveApplicants() {
        ApiResponse<?>paid = savingsService.getSavingsApproveApplicants();
        return ResponseEntity.ok(paid);
    }


}
