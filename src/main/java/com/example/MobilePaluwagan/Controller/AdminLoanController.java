package com.example.MobilePaluwagan.Controller;

import com.example.MobilePaluwagan.DTOs.Request.AdminApplicantRequest;
import com.example.MobilePaluwagan.DTOs.Response.ApiResponse;
import com.example.MobilePaluwagan.DTOs.Response.ApplicantsFullInfoAdmin;
import com.example.MobilePaluwagan.DTOs.Response.LoanApplicantsAdmin;
import com.example.MobilePaluwagan.Service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/admin")
public class AdminLoanController {
    @Autowired
    LoanService loanService;

    @GetMapping("/loan/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<LoanApplicantsAdmin>>> getAllPendingApplications() {
        ApiResponse<List<LoanApplicantsAdmin>> pending = loanService.getPendingApplicants();
        return ResponseEntity.ok(pending);
    }

    @GetMapping("/loan/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<LoanApplicantsAdmin>>> getAllApproveApplications() {
        ApiResponse<List<LoanApplicantsAdmin>> approve = loanService.getApproveApplicants();
        return ResponseEntity.ok(approve);
    }

    @GetMapping("/loan/rejected")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<LoanApplicantsAdmin>>> getAllRejectedApplications() {
        ApiResponse<List<LoanApplicantsAdmin>> rejected = loanService.getRejectedApplicants();
        return ResponseEntity.ok(rejected);
    }

    @GetMapping("/loan/details/{applicationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApplicantsFullInfoAdmin> getAllApproveApplications(@PathVariable Long applicationId) {
        System.out.println("Searching for applicationID: " + applicationId);
        ApplicantsFullInfoAdmin applicantsFullInfo = loanService.applicantsFullInfo(applicationId);
        System.out.println("Query result: " + applicantsFullInfo);
        return ResponseEntity.ok(applicantsFullInfo);
    }

//    @PostMapping("/loan/approve/{applicationId}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<?>

}
