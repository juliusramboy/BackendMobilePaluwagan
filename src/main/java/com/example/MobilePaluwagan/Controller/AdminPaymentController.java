package com.example.MobilePaluwagan.Controller;

import com.example.MobilePaluwagan.DTOs.Request.PaymentLoanRequest;
import com.example.MobilePaluwagan.DTOs.Response.*;
import com.example.MobilePaluwagan.Entity.DueDateSchedule;
import com.example.MobilePaluwagan.Service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("api/admin/payment/")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaymentController {

    @Autowired
    PaymentService paymentService;

    @PostMapping("/loan")
    public ApiResponse<?> sendPaymentLoan(@RequestBody PaymentLoanRequest paymentLoanRequest) {
        ApiResponse<?> pay = paymentService.processPayment(paymentLoanRequest);
        return pay;
    }

    @GetMapping("/loan/search")
    public ResponseEntity<LoanApplicationResponseAdmin> searchLoanApplicant(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(paymentService.searchLoanApplicant(name, page, size));
    }

    @GetMapping("/savings/search")
    public ResponseEntity<SavingsApplicationResponseAdmin> searchSavingsApplicant(
           @RequestParam String name,
           @RequestParam(defaultValue =  "0") int page,
           @RequestParam(defaultValue =  "10") int size
    ){

        return ResponseEntity.ok(paymentService.searchSavingsApplicant(name, page, size));
    }
}
