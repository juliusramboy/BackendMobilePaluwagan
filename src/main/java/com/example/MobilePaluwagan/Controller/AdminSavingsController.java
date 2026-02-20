package com.example.MobilePaluwagan.Controller;

import com.example.MobilePaluwagan.DTOs.Response.ApiResponse;
import com.example.MobilePaluwagan.Service.SavingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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


    @GetMapping("/members")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> getAllPendingApplicants() {
        ApiResponse<?>pending = savingsService.getSavingsPendingApplicants();
        return ResponseEntity.ok(pending);
    }
    


}
