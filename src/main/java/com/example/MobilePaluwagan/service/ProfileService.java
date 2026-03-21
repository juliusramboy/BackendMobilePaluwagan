package com.example.MobilePaluwagan.service;

import com.example.MobilePaluwagan.controller.SseController;
import com.example.MobilePaluwagan.dto.Request.ProfileUpdateRequest;
import com.example.MobilePaluwagan.dto.Response.ApiResponse;
import com.example.MobilePaluwagan.dto.Response.UserProfileResponse;
import com.example.MobilePaluwagan.entity.*;
import com.example.MobilePaluwagan.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserInfoRepo userInfoRepo;
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final SseController sseController;
    private final UserLoanRepo userLoanRepo;
    private final UserSavingsRepo userSavingsRepo;
    private final UserBankRepo userBankRepo;
    private final DueDateScheduleRepository dueDateScheduleRepo;
    private final LoanPaymentRepo  loanPaymentRepo;
    private final LedgerRepo  ledgerRepo;


    public UserProfileResponse userAllInfo(Long userId) {
        UserInfo userInfo = userInfoRepo.findByUserId(userId).orElseThrow(() -> new RuntimeException("User info not found"));
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Optional<UserBank> userBank = userBankRepo.findByUserId(userId);
        Optional<Loan> payment = userLoanRepo.findByUserId(userId);

        boolean isMatured = false;
        BigDecimal accountBalance = BigDecimal.ZERO;

        if (userBank.isPresent()){
            UserBank bank = userBank.get();

            accountBalance = bank.getAccountBalance();
            isMatured = Optional.ofNullable(bank.getFirstDepositDate())
                    .map(date -> !date.plusYears(1).toLocalDate().isAfter(LocalDate.now()))
                    .orElse(false);;
        }


        BigDecimal loanBalance = BigDecimal.ZERO;
        LocalDate loanDueDate = null;

       if(payment.isPresent()) {
           Loan UserPayment = payment.get();
           Optional<DueDateSchedule> loanDue = dueDateScheduleRepo
                   .findFirstPartialOrPending(
                           UserPayment.getApplicationID()
                   );

           // ✅ Fix 1 — correct loan balance
           loanBalance = UserPayment.getTotalRepayable()
                   .subtract(UserPayment.getLoanRepaymentTally());

           loanDueDate = loanDue
                   .map(DueDateSchedule::getDueDate)
                   .orElse(null);
       }


        return new UserProfileResponse(
                userInfo.getFirstName(),
                userInfo.getLastName(),
                userInfo.getMiddleName(),
                userInfo.getSuffix(),
                userInfo.getPhoneNumber(),
                userInfo.getVerifiedDate(),
                userInfo.getAddress(),
                userInfo.getBirthDay(),
                userInfo.getGender(),
                userInfo.getProfileImage(),
                user.isOnline(),
                user.getEmail(),
                accountBalance,
                isMatured,
                loanBalance,
                loanDueDate
        );
    }

    @Transactional
    public ApiResponse<String> updateProfile(Long userId, ProfileUpdateRequest request) {


        UserInfo info = userInfoRepo.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id: " + userId
                ));

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id: " + userId
                ));



        if (request.getFirstName() != null) {
            info.setFirstName(request.getFirstName());
        }

        if (request.getMiddleName() != null) {
            info.setMiddleName(request.getMiddleName());
        }

        if (request.getLastName() != null) {
            info.setLastName(request.getLastName());
        }

        if (request.getSuffix() != null) {
            info.setSuffix(request.getSuffix());
        }

        if (request.getGender() != null) {
            info.setGender(request.getGender());
        }

        if (request.getAddress() != null) {
            info.setAddress(request.getAddress());
        }

        if (request.getBirthDay() != null) {
            info.setBirthDay(request.getBirthDay());
        }

        if (request.getPhoneNumber() != null) {
            info.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getEmail() != null){
            user.setEmail(request.getEmail());
        }

        if (request.getNewPassword() != null && request.getOldPassword() != null){

            if (passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
                    String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());

                    user.setPassword(encodedNewPassword);

                    userRepo.save(user);
                } else {
                    return new ApiResponse<>(
                            false,
                            "New password cannot be null or blank",
                            null
                    );
                }
            } else {
                return new ApiResponse<>(
                        false,
                        "Old password does not match",
                        null
                );
            }

        }

        UserInfo savedUser = userInfoRepo.save(info);

        sseController.notifyUpdate();
        return new ApiResponse<>(
                true,
                "Successfully updated profile",
                "Profile updated"
        );
    }

    public Page<Ledger> getAllPayments(Long userId, int page, int size) {
        return ledgerRepo.findAllByUserId(userId, PageRequest.of(page, size));
    }





}
