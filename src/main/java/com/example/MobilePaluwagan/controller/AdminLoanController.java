package com.example.MobilePaluwagan.controller;

import com.example.MobilePaluwagan.dto.Request.AdminLoanStatus;
import com.example.MobilePaluwagan.dto.Response.ApiResponse;
import com.example.MobilePaluwagan.dto.Response.ApplicantsFullInfoAdmin;
import com.example.MobilePaluwagan.dto.Response.LoanApplicationResponse;
import com.example.MobilePaluwagan.entity.Status;
import com.example.MobilePaluwagan.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/admin/loan")
public class AdminLoanController {
    @Autowired
    LoanService loanService;

    @GetMapping("/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanApplicationResponse> getAllPendingApplications(@PathVariable Status status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(loanService.getPendingApplicants(status, page, size));
    }


    @GetMapping("/details/{applicationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApplicantsFullInfoAdmin> getAllApproveApplications(@PathVariable Long applicationId) {
        System.out.println("Searching for applicationID: " + applicationId);
        ApplicantsFullInfoAdmin applicantsFullInfo = loanService.applicantsFullInfo(applicationId);
        System.out.println("Query result: " + applicantsFullInfo);
        return ResponseEntity.ok(applicantsFullInfo);
    }


    @PutMapping("/change-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> changeLoanStatus(@RequestBody AdminLoanStatus request){
        return loanService.loanAdminChangeStats(request);
    }

    @GetMapping("/status-counts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getStatusCounts(){
        Map<String, Long> counts = loanService.getStatusCounts();
        return ResponseEntity.ok(counts);
    }

}
