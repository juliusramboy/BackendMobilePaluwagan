package com.example.MobilePaluwagan.Service;

import com.example.MobilePaluwagan.Controller.LoanSseController;
import com.example.MobilePaluwagan.DTOs.Request.AdminSavingsStatus;
import com.example.MobilePaluwagan.DTOs.Request.PaymentFilterRequest;
import com.example.MobilePaluwagan.DTOs.Request.PaymentFilterRequestAdmin;
import com.example.MobilePaluwagan.DTOs.Response.*;
import com.example.MobilePaluwagan.Entity.*;
import com.example.MobilePaluwagan.Repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Random;
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

    @Autowired
    private SavingsWithdrawApplicationRepo savingsWithdrawApplicationRepo;

    @Transactional
    public ApiResponse<?> adminAcceptPayment(AdminSavingsStatus request) {
        UserBank userBank = userBankRepo.findBySavingsId(request.getSavingsId());
        List<UserSavings> userSavings = userSavingsRepo.findByUserId(userBank.getUserId());

        Optional<UserSavings> savingsWithReference = userSavings.stream()
                .filter(s -> request.getReference().equals(s.getReference()))
                .findFirst();

        if (savingsWithReference.isPresent() && savingsWithReference.get().getStatus() == Status.PENDING) {
            Status status = savingsWithReference.get().getStatus();
            UserSavings reference = savingsWithReference.get();

            if (request.getStatus() == status){
                return new ApiResponse<>(true, "Payment already processed", null);
            }
            if (request.getStatus() == Status.REJECTED) {
                userSavingsRepo.delete(reference);
                loanSseController.notifyLoan();
                return new ApiResponse<>(true, "Payment deleted", null);
            }

            if (userBank.getFirstDepositDate() == null) {
                userBank.setFirstDepositDate(reference.getDepositDate());
                userBank.setHasSavingsDeposit(true);

                userBankRepo.save(userBank);
            }

            BigDecimal addPayment = userBank.getAccountBalance().add(BigDecimal.valueOf(reference.getAmountDeposit()));
            userBank.setAccountBalance(addPayment);
            userBankRepo.save(userBank);
            reference.setStatus(Status.PAID);
            userSavingsRepo.save(reference);
            loanSseController.notifyLoan();
            return new ApiResponse<>(true, "Payment Added to User", null);

        }else {
            return new ApiResponse<>(false, "Payment already processed", null);
        }

    }


    public ApiResponse<UserDepositSavingsResponse>  userDeposit(Long userId, double depositAmount, LocalDate depositDate){
        UserBank user = userBankRepo.findByUserId(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "userId not found"));

        ApiResponse<UserDepositSavingsResponse> x = checkUserDepositInput(userId, depositAmount, depositDate);
        if (x != null) return x;

        LocalDateTime depositDateTime = depositDate.atTime(LocalTime.now());

            UserSavings deposit;
                UserSavings userSavings = new UserSavings();
                userSavings.setUserId(userId);
                userSavings.setSavingsId(user.getSavingsId());
                userSavings.setAmountDeposit(depositAmount);
                userSavings.setDepositDate(depositDateTime);
                userSavings.setReference(generateRef());
                userSavings.setStatus(Status.PENDING);

                deposit = userSavingsRepo.save(userSavings);

                loanSseController.notifyLoan();

            
            UserDepositSavingsResponse responseData = mapToUserSavingsResponse(deposit);

            return new ApiResponse<>(
                    true,
                    "Deposit created successfully",
                    responseData
            );


    }

    public ApiResponse<?> getAllSavingsMembers(){
        return new ApiResponse<>(
                true,
                "Successful",
                userSavingsRepo.findAllMembers()
        );
    }

    public ApiResponse<?> getAllPendingPayments(String savingsId){
        UserBank user = userBankRepo.findBySavingsId(savingsId);

        if (user == null){
            return new ApiResponse<>(
                    false,
                    "Savings Id not exist in records",
                    null
            );
        }

        UserInfo userInfo = userInfoRepo.findByUserId(user.getUserId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "userid not found"));
        SavingsWithdrawApplication savingsWithdrawApplication = savingsWithdrawApplicationRepo.findByUserId(user.getUserId());

        WithdrawSavingsInfo withdrawSavingsInfo = WithdrawSavingsInfo.builder()
                .withdrawDate(savingsWithdrawApplication.getWithdrawDate())
                .reference(savingsWithdrawApplication.getReference())
                .build();

        UserSavingsInfo userSavingsInfo = UserSavingsInfo.builder()
                .firstName(userInfo.getFirstName())
                .lastName(userInfo.getLastName())
                .targetAmount(user.getTargetAmount())
                .savingsId(user.getSavingsId())
                .accountBalance(user.getAccountBalance())
                .maturityDate(user.getFirstDepositDate().plusYears(1))
                .build();

        List<SavingsPendingPaymentMemberResponse> payments = userBankRepo.findPendingPaymentBySavingsId(savingsId);

        return new ApiResponse<>(
                true,
                "Success",
                SavingsDetailResponse.builder()
                        .user(userSavingsInfo)
                        .payments(payments)
                        .build()

        );
    }

    public ApiResponse<?> getSavingsApproveApplicants(){
        return new ApiResponse<>(
                true,
                "Successful",
                userSavingsRepo.findAllMembers()
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
                .remitDate(savings.getDepositDate().toLocalDate())
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

        UserBank userBank = userBankRepo.findByUserId(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "userId is not found"));

         String savingsId = userBank.getSavingsId();
         BigDecimal targetAmount = userBank.getTargetAmount();

         BigDecimal userTargetAmount = userBank.getTargetAmount();

        List<SavingsDepositHistory> savingsDepositHistoryList = userSavings.stream()
                .filter(status -> status.getStatus() == Status.PAID)
                .map(this::allHistory)
                .collect(Collectors.toList());

        BigDecimal totalSavings = userSavings.stream()
                .filter(amount -> "PAID".equalsIgnoreCase(amount.getStatus().name()))
                .map(amount -> BigDecimal.valueOf(amount.getAmountDeposit()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal savingsBase = new BigDecimal("5000");
        BigDecimal annualBase = new BigDecimal("500");

        BigDecimal maxAnnual = targetAmount
                .divide(savingsBase, 10, RoundingMode.HALF_UP)
                .multiply(annualBase)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal savingsForAnnual = totalSavings
                .min(targetAmount)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal userAnnual = savingsForAnnual
                .divide(savingsBase, 10, RoundingMode.HALF_UP)
                .multiply(annualBase)
                .min(maxAnnual)
                .setScale(2, RoundingMode.HALF_UP);

        boolean isTargetReached = totalSavings.compareTo(userBank.getTargetAmount()) >= 0;



        System.out.println(totalSavings);
        System.out.println(userAnnual);


        SavingsSummaryResponse response = new SavingsSummaryResponse();
        response.setSavingsId(savingsId);
        response.setTotalSavingsBalance(totalSavings);
        response.setTargetAmount(userTargetAmount);
        response.setDepositHistoryList(savingsDepositHistoryList);
        response.setTargetReached(isTargetReached);
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

        ApiResponse<?> userInfo1 = savingsValidation(targetAmount, userInfo, userBank);
        if (userInfo1 != null) return userInfo1;


        userBank.setSavingsId(savingsId());
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

    private static ApiResponse<?> savingsValidation(BigDecimal targetAmount, UserInfo userInfo, UserBank userBank) {
        if (userInfo.getAddress() == null || userInfo.getBirthDay() == null|| userInfo.getFirstName() == null || userInfo.getGender() == null || userInfo.getLastName() == null || userInfo.getMiddleName() == null || userInfo.getPhoneNumber() == null || userInfo.getSuffix() == null) {
            return new ApiResponse<>(
                    false,
                    "Pls complete your details in profile",
                    null
            );
        }

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
                    null);
        }
        return null;
    }

    private SavingsDepositHistory allHistory(UserSavings savings) {
        return SavingsDepositHistory.builder()
                .amountRemit(savings.getAmountDeposit())
                .remitDate(savings.getDepositDate())
                .reference(savings.getReference())
                .status(savings.getStatus().name())
                .build();

    }

    public ApiResponse<?> withdrawSavings(Long userId) {
        UserBank user = userBankRepo.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        boolean hasApplication  = savingsWithdrawApplicationRepo
                .existsByUserIdAndStatus(userId,Status.PENDING);

        if (hasApplication){
            return new ApiResponse<>(
                    false,
                    "We see that you applied to withdraw your savings. Please wait for admin to approve it.",
                    null
            );
        }

        LocalDateTime firstDepositDate = user.getFirstDepositDate();
        LocalDateTime oneYearLater = firstDepositDate.plusYears(1);
        LocalDateTime now = LocalDateTime.now();

        boolean hasBeenOneYear = now.isAfter(oneYearLater)|| now.isEqual(oneYearLater);

        boolean isTargetReached = user.getAccountBalance()
                .compareTo(user.getTargetAmount()) >= 0;

        BigDecimal savingsBase = new BigDecimal("5000");
        BigDecimal annualBase = new BigDecimal("500");
        BigDecimal annual;


        if (isTargetReached && hasBeenOneYear) {
            BigDecimal maxAnnual = user.getTargetAmount()
                    .divide(savingsBase, 10, RoundingMode.HALF_UP)
                    .multiply(annualBase);

            annual = user.getAccountBalance()
                    .divide(savingsBase, 10, RoundingMode.HALF_UP)
                    .multiply(annualBase)
                    .min(maxAnnual)
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            annual = BigDecimal.ZERO;
        }

        SavingsWithdrawApplication withdrawApplication = new SavingsWithdrawApplication();
        withdrawApplication.setSavingsId(user.getSavingsId());
        withdrawApplication.setAnnual(annual);
        withdrawApplication.setWithdrawDate(LocalDateTime.now());
        withdrawApplication.setUserId(user.getUserId());
        withdrawApplication.setTargetAmount(user.getTargetAmount());
        withdrawApplication.setAccountBalance(user.getAccountBalance());
        withdrawApplication.setReference(generateRef());
        withdrawApplication.setStatus(Status.WITHDRAW);

        savingsWithdrawApplicationRepo.save(withdrawApplication);

        BigDecimal totalWithdrawal = user.getAccountBalance().add(annual);

        WithdrawApplicationResponse response = WithdrawApplicationResponse.builder()
                .savingsId(withdrawApplication.getSavingsId())
                .totalWithdrawal(totalWithdrawal)
                .reference(withdrawApplication.getReference())
                .status(withdrawApplication.getStatus().name())
                .build();

        String message;
        if (!hasBeenOneYear) {
            long daysRemaining = ChronoUnit.DAYS.between(now, oneYearLater);
            message = "Successfully applied for withdrawal. No annual bonus yet - please wait " + daysRemaining + " more days (1 year requirement).";
        } else if (!isTargetReached) {
            message = "Successfully applied for withdrawal. No annual bonus (target not reached).";
        } else {
            message = "Successfully applied for withdrawal with ₱" + annual + " annual bonus!";
        }

        return new ApiResponse<>(
                true,
                message,
                response
        );
    }

    public List<UserSavings> filterSavingsPayment(PaymentFilterRequest filter){
        return userSavingsRepo.findPaymentsByUserIdWithFilters(
                filter.getUserId(),
                filter.getReference(),
                filter.getStartDate(),
                filter.getEndDate(),
                Status.PAID.name()
        );
    }

    public SavingsDepositHistory convertToDto(UserSavings savings){
        return  SavingsDepositHistory.builder()
                .amountRemit(savings.getAmountDeposit())
                .remitDate(savings.getDepositDate())
                .reference(savings.getReference())
                .status(savings.getStatus().name())
                .build();
    }

    public List<UserSavings> filterSavingsPaymentAdmin(PaymentFilterRequestAdmin filter){
        return userSavingsRepo.findPaymentUserSavingsIdFilter(
                filter.getSavingsId(),
                filter.getReference(),
                filter.getStartDate(),
                filter.getEndDate(),
                Status.PAID.name()
        );
    }

    public SavingsDepositHistory convertToDtoAdmin(UserSavings savings){
        return  SavingsDepositHistory.builder()
                .amountRemit(savings.getAmountDeposit())
                .remitDate(savings.getDepositDate())
                .reference(savings.getReference())
                .status(savings.getStatus().name())
                .build();
    }

    public String savingsId () {
        String prefix = "SID";
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();

        String letter1 = String.valueOf(randomLetters.charAt(random.nextInt(26)));
        String letter2 = String.valueOf(randomLetters.charAt(random.nextInt(26)));
        String letter3 = String.valueOf(randomLetters.charAt(random.nextInt(26)));

        Optional<UserSavings> lastRef = userSavingsRepo.findLastRef();

        int sequence = 1;

        if(lastRef.isPresent()){
            String lastRefNumber = lastRef.get().getReference();
            String lastSequence = lastRefNumber.substring(13, 17);
            sequence = Integer.parseInt(lastSequence) + 1;
        }

        String sequencePart = String.format("%04d", sequence);

        return prefix + datePart + letter1 + letter2 + sequencePart + letter3;
    }

    public String generateRef() {
        String prefix = "REF";
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();

        String letter1 = String.valueOf(randomLetters.charAt(random.nextInt(26)));
        String letter2 = String.valueOf(randomLetters.charAt(random.nextInt(26)));
        String letter3 = String.valueOf(randomLetters.charAt(random.nextInt(26)));

        Optional<UserSavings> lastRef = userSavingsRepo.findLastRef();

        int sequence = 1;

        if(lastRef.isPresent()){
            String lastRefNumber = lastRef.get().getReference();

            if (lastRefNumber.length() >= 17) {
                try {
                    String lastSequence = lastRefNumber.substring(13, 17);
                    sequence = Integer.parseInt(lastSequence) + 1;
                } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                    System.err.println("Error parsing reference: " + lastRefNumber);
                    sequence = 1;
                }
            }
        }

        String sequencePart = String.format("%04d", sequence);

        return prefix + datePart + letter1 + letter2 + sequencePart + letter3;
    }

}
