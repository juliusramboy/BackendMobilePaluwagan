package com.example.MobilePaluwagan.controller;

import com.example.MobilePaluwagan.annotation.Idempotent;
import com.example.MobilePaluwagan.dto.Request.PaymentAdminRequest;
import com.example.MobilePaluwagan.dto.Response.*;
import com.example.MobilePaluwagan.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/admin/payment")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaymentController {

    @Autowired
    PaymentService paymentService;

    @PostMapping("/loan")
    @Idempotent
    public ApiResponse<?> sendPaymentLoan(@RequestBody PaymentAdminRequest paymentAdminRequest) {
        ApiResponse<?> loanPayment = paymentService.processLoanPayment(paymentAdminRequest);
        return loanPayment;
    }

    @PostMapping("/savings")
    @Idempotent
    public ApiResponse<?> sendPayment(@RequestBody PaymentAdminRequest paymentAdminRequest) {
        ApiResponse<?> savingsPayment = paymentService.processSavingsPayment(paymentAdminRequest);
        return savingsPayment;
    }

    @GetMapping("/loan/search")
    public ResponseEntity<LoanApplicationResponseAdmin> searchLoanApplicant(
            @RequestParam (required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        return ResponseEntity.ok(paymentService.searchLoanApplicant(name, page, size));
    }

    @GetMapping("/savings/search")
    public ResponseEntity<SavingsApplicationResponseAdmin> searchSavingsApplicant(
           @RequestParam String name,
           @RequestParam(defaultValue =  "0") int page,
           @RequestParam(defaultValue =  "5") int size
    ){

        return ResponseEntity.ok(paymentService.searchSavingsApplicant(name, page, size));
    }
}
