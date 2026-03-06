package com.example.MobilePaluwagan.Controller;

import com.example.MobilePaluwagan.DTOs.Request.PaymentLoanRequest;
import com.example.MobilePaluwagan.DTOs.Response.AdminPaymentLoanSearchResponse;
import com.example.MobilePaluwagan.DTOs.Response.ApiResponse;
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
    public ResponseEntity<List<AdminPaymentLoanSearchResponse>> searchApplicant(@RequestParam String name){
        List<AdminPaymentLoanSearchResponse> result = paymentService.searchApplicant(name);

        if(result.isEmpty()){
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(result);
    }
}
