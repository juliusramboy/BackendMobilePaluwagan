package com.example.MobilePaluwagan.Controller;

import com.example.MobilePaluwagan.DTOs.Response.LoanApplicantsAdmin;
import com.example.MobilePaluwagan.Entity.LoanApplication;
import com.example.MobilePaluwagan.Repository.LoanApplicationRepo;
import com.example.MobilePaluwagan.Service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/admin")
public class AdminLoanController {
    @Autowired
    LoanService loanService;

    @GetMapping("/loan/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LoanApplicantsAdmin>> getAllPendingLoan() {
        List<LoanApplicantsAdmin> pending = loanService.getPendingApplicants();
        return ResponseEntity.ok(pending);
    }

    @GetMapping("/loan/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LoanApplicantsAdmin>> getAllApproveLoan(){
        List<LoanApplicantsAdmin> approve = loanService.getApproveApplicants();
        return ResponseEntity.ok(approve);
    }
  }
