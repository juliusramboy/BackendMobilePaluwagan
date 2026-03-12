package com.example.MobilePaluwagan.Controller;

import com.example.MobilePaluwagan.DTOs.Request.PaymentAdminRequest;
import com.example.MobilePaluwagan.DTOs.Response.*;
import com.example.MobilePaluwagan.Service.PaymentService;
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
    public ApiResponse<?> sendPaymentLoan(@RequestBody PaymentAdminRequest paymentAdminRequest) {
        ApiResponse<?> loanPayment = paymentService.processLoanPayment(paymentAdminRequest);
        return loanPayment;
    }

    @PostMapping
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
