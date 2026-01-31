package com.example.MobilePaluwagan.Controller;

import com.example.MobilePaluwagan.DTOs.Response.LoanApplicantsAdmin;
import com.example.MobilePaluwagan.Repository.LoanApplicationRepo;
import com.example.MobilePaluwagan.Service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/admin")
public class AdminLoanController {
//    @Autowired
//    LoanService loanService;

//    @GetMapping("/loan/all-applicants")
//    public ResponseEntity<?> showAllApplicants(Authentication authentication){
//
//    }

//    @GetMapping("/pending")
//    public ResponseEntity<List<LoanApplicantsAdmin>> showAllPendingApplicants() {
//        List<LoanApplicantsAdmin> applicants = loanService.getAllpending(); return ResponseEntity.ok(applicants); }
  }
