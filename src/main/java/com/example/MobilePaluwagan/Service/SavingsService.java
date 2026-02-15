package com.example.MobilePaluwagan.Service;

import com.example.MobilePaluwagan.Controller.LoanSseController;
import com.example.MobilePaluwagan.DTOs.Response.ApiResponse;
import com.example.MobilePaluwagan.DTOs.Response.SavingsDepositHistory;
import com.example.MobilePaluwagan.DTOs.Response.SavingsSummaryResponse;
import com.example.MobilePaluwagan.DTOs.Response.UserDepositSavingsResponse;
import com.example.MobilePaluwagan.Entity.*;
import com.example.MobilePaluwagan.Repository.UserBankRepo;
import com.example.MobilePaluwagan.Repository.UserInfoRepo;
import com.example.MobilePaluwagan.Repository.UserRepo;
import com.example.MobilePaluwagan.Repository.UserSavingsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SavingsService {

    @Autowired
    private UserSavingsRepo userSavingsRepo;

    @Autowired
    private UserBankRepo userBankRepo;

    @Autowired
    private UserInfoRepo userInfoRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private LoanSseController loanSseController;


    public ApiResponse<UserDepositSavingsResponse> userDeposit(Long userId, double depositAmount, LocalDate depositDate){
        Optional<UserBank> userBank = userBankRepo.findByUserId(userId);

        ApiResponse<UserDepositSavingsResponse> x = checkUserDepositInput(userId, depositAmount, depositDate);
        if (x != null) return x;

        UserBank user = userBank.get();
            UserSavings deposit;
                UserSavings userSavings = new UserSavings();
                userSavings.setUserId(userId);
                userSavings.setAmountDeposit(depositAmount);
                userSavings.setDepositDate(depositDate);
                userSavings.setStatus(Status.PENDING);

                deposit = userSavingsRepo.save(userSavings);

            
            UserDepositSavingsResponse responseData = mapToUserSavingsResponse(deposit);

            return new ApiResponse<>(
                    true,
                    "Deposit created successfully",
                    responseData
            );


    }

    private ApiResponse<UserDepositSavingsResponse> checkUserDepositInput(Long userId, double depositAmount, LocalDate depositDate) {
        if (depositAmount <= 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Deposit amount must be greater than zero"
            );
        }

        if(depositDate == null){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Deposit date cannot be null"
            );
        }

        if(userSavingsRepo.existsByUserIdAndStatus(userId, Status.PENDING)){
            return new ApiResponse<>(
                    false,
                    "We see that you have a pending deposit. Please wait for the admin to process it.",
                    null
            );
        }
        return null;
    }

    private UserDepositSavingsResponse mapToUserSavingsResponse(UserSavings savings){
        return UserDepositSavingsResponse.builder()
                .amountRemit(savings.getAmountDeposit())
                .remitDate(savings.getDepositDate())
                .reference(savings.getReference())
                .status(savings.getStatus().name())
                .build();

    }

    public boolean depositChecker(Long userId){
        boolean userSavings = userSavingsRepo.findByUserId(userId).stream()
                .anyMatch(checker -> checker.getStatus() == Status.PENDING);

        return userSavings;
    }

    public ApiResponse<SavingsSummaryResponse> savingsAllData(Long userId){
        List<UserSavings> userSavings = userSavingsRepo.findByUserId(userId);

        if(userSavings.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user does not have savings record");
        }


        List<SavingsDepositHistory> savingsDepositHistoryList = userSavings.stream()
                .map(this::allHistory)
                .collect(Collectors.toList());

        double totalSavings = userSavings.stream()
                .filter(amount -> "PAID".equalsIgnoreCase(amount.getStatus().name()))
                .mapToDouble(UserSavings::getAmountDeposit)
                .sum();

        double savingsBase = 5000;
        double annualBase = 500;
        double userAnnual = 0.00;

        if (totalSavings > 0) {
             double divMoney = Math.floor(totalSavings / savingsBase);

            if(divMoney > 0){
                userAnnual = divMoney * annualBase;
            }
        }

        System.out.println(totalSavings);
        System.out.println(userAnnual);



        SavingsSummaryResponse response = new SavingsSummaryResponse();
        response.setTotalSavingsBalance(totalSavings);;
        response.setDepositHistoryList(savingsDepositHistoryList);
        response.setAnnualMoney(userAnnual);


        return new ApiResponse<>(
                true,
                "Data retrieved successfully",
                response
        );
    }

    public ApiResponse<?> createSavingsAcc(Long userId, BigDecimal targetAmount, String sourceOfFunds){

        UserBank userBank = userBankRepo.findByUserId(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "userId is not found"));
        UserInfo userInfo = userInfoRepo.findByUserId(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "userId not found"));
        User user = userRepo.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "userId not found"));


        if (targetAmount.compareTo(BigDecimal.valueOf(5000)) < 0) {
            return new ApiResponse<>(
                    false,
                    "5000 is minimum target amount in Savings",
                    targetAmount
            );
        }

        if(userBank.getTargetAmount() != null){
            return new ApiResponse<>(
                    false,
                    "You already set a target amount",
                    null );
        }


        userBank.setSavingsId(savingsId(userId));
        userBank.setTargetAmount(targetAmount);
        user.setHasSavingsAccount(true);

        userInfo.setSourceOfFunds(sourceOfFunds);

        userInfoRepo.save(userInfo);

        userBankRepo.save(userBank);

        loanSseController.notifyLoan();

        return  new ApiResponse<>(
                true,
                "Successfully applied Savings",
                userBank
        );
    }

    private SavingsDepositHistory allHistory(UserSavings savings) {
        return SavingsDepositHistory.builder()
                .amountRemit(savings.getAmountDeposit())
                .remitDate(savings.getDepositDate())
                .reference(savings.getReference())
                .status(savings.getStatus().name())
                .build();

    }

    public String savingsId (Long userId) {
        String prefix = "SID";
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String userIdPart = "0" + userId;
        return prefix + datePart + userIdPart;
    }

}
