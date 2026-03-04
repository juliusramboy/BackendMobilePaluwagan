package com.example.MobilePaluwagan.Controller;

import com.example.MobilePaluwagan.DTOs.Request.PaymentLoanRequest;
import com.example.MobilePaluwagan.DTOs.Response.ApiResponse;
import com.example.MobilePaluwagan.Entity.DueDateSchedule;
import com.example.MobilePaluwagan.Service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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
}
