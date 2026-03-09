package com.example.MobilePaluwagan.Controller;

import com.example.MobilePaluwagan.DTOs.Request.AdminSavingsStatus;
import com.example.MobilePaluwagan.DTOs.Request.PaymentFilterRequest;
import com.example.MobilePaluwagan.DTOs.Request.PaymentFilterRequestAdmin;
import com.example.MobilePaluwagan.DTOs.Request.SavingsResponseAdmin;
import com.example.MobilePaluwagan.DTOs.Response.AdminTallySavings;
import com.example.MobilePaluwagan.DTOs.Response.ApiResponse;
import com.example.MobilePaluwagan.DTOs.Response.SavingsDepositHistory;
import com.example.MobilePaluwagan.DTOs.Response.SavingsResponse;
import com.example.MobilePaluwagan.Entity.UserSavings;
import com.example.MobilePaluwagan.Service.SavingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/admin/savings")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSavingsController {

    @Autowired
    private SavingsService savingsService;


    @PostMapping("/payment")
    public ResponseEntity<ApiResponse<?>> acceptPendingPayments(@RequestBody AdminSavingsStatus request){
        ApiResponse<?> payment = savingsService.adminAcceptPayment(request);
        return ResponseEntity.ok(payment);
    }
    @GetMapping("/members")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> getAllSavingsMembers() {
        ApiResponse<?>members = savingsService.getAllSavingsMembers();
        return ResponseEntity.ok(members);
    }

    @GetMapping("/total")
    public ResponseEntity<AdminTallySavings> getAllSavingsAccountBalance() {
        AdminTallySavings tally = savingsService.getTallySavings();

        return ResponseEntity.ok(tally);
    }

    @GetMapping("/members/{savingsId}")
    public ResponseEntity<ApiResponse<?>> getAllPendingPayments(@PathVariable String savingsId) {
        ApiResponse<?> pendingPayments = savingsService.getAllPendingPayments(savingsId);
        return ResponseEntity.ok(pendingPayments);
    }

    @GetMapping("/payment/filter/{savingsId}")
    public ResponseEntity<SavingsResponseAdmin> filterSavings(
            @PathVariable String savingsId,
            @RequestParam(required = false) String reference,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate endDate) {

        PaymentFilterRequestAdmin filterRequestAdmin = PaymentFilterRequestAdmin.builder()
                .savingsId(savingsId)
                .reference(reference)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        List<UserSavings> savings = savingsService.filterSavingsPaymentAdmin(filterRequestAdmin);
        List<SavingsDepositHistory> depositHistoryList = savings.stream()
                .map(savingsService::convertToDtoAdmin)
                .toList();

        SavingsResponseAdmin response = SavingsResponseAdmin.builder()
                .success(true)
                .message("Successfully retrieved payments")
                .filters(filterRequestAdmin.hasFilters() ? filterRequestAdmin : null)
                .savings(depositHistoryList)
                .build();

        return ResponseEntity.ok(response);
    }




}
