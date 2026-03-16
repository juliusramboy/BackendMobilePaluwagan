package com.example.MobilePaluwagan.service;

import com.example.MobilePaluwagan.dto.Response.StatusResponse;
import com.example.MobilePaluwagan.entity.*;
import com.example.MobilePaluwagan.repository.LoanApplicationRepo;
import com.example.MobilePaluwagan.repository.UserBankRepo;
import com.example.MobilePaluwagan.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class StatusService {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private LoanApplicationRepo loanApplicationRepo;
    @Autowired
    private UserBankRepo userBankRepo;

    public StatusResponse getUserStatus(Long userId){

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        UserBank userBank = userBankRepo.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        boolean hasActiveLoan = user.isHasLoan();
        boolean hasSavingsAccount = user.isHasSavingsAccount();
        boolean hasActiveSavings = userBank.isHasSavingsDeposit();

        String SavingsId = userBank.getSavingsId();



        boolean hasPendingApplication = loanApplicationRepo.existsByUserIdAndStatusIn(
                userId,
                List.of(Status.PENDING)
        );

        boolean hasApprovedApplication = loanApplicationRepo.existsByUserIdAndStatusIn(
                userId,
                List.of(Status.APPROVED)
        );

        Optional<LoanApplication> latestApplication = loanApplicationRepo.findAllByUserId(userId).stream()
                .filter(app -> app.getStatus() == Status.PENDING || app.getStatus() == Status.APPROVED)
                .max(Comparator.comparing(LoanApplication::getApplicationID));

        return new StatusResponse(
                hasActiveLoan,
                hasActiveSavings,
                hasSavingsAccount,
                hasPendingApplication,
                hasApprovedApplication,
                latestApplication.map(LoanApplication::getApplicationID).orElse(null),
                SavingsId,
                latestApplication.map(app -> app.getStatus().name()).orElse(null)
        );
    }
}
