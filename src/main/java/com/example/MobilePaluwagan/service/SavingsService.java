package com.example.MobilePaluwagan.service;

import com.example.MobilePaluwagan.controller.SseController;
import com.example.MobilePaluwagan.dto.Request.AdminSavingsStatus;
import com.example.MobilePaluwagan.dto.Request.PaymentFilterRequest;
import com.example.MobilePaluwagan.dto.Request.PaymentFilterRequestAdmin;
import com.example.MobilePaluwagan.dto.Response.*;
import com.example.MobilePaluwagan.entity.*;
import com.example.MobilePaluwagan.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private SseController sseController;

    @Autowired
    private SavingsWithdrawApplicationRepo savingsWithdrawApplicationRepo;

    @Autowired
    private LedgerRepo ledgerRepo;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public ApiResponse<?> adminAcceptPayment(AdminSavingsStatus request) {
        UserBank userBank = userBankRepo.findBySavingsId(request.getSavingsId());
        List<UserSavings> userSavings = userSavingsRepo.findByUserId(userBank.getUserId());
        UserInfo info = userInfoRepo.findByUserId(userBank.getUserId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        Optional<UserSavings> savingsWithReference = userSavings.stream()
                .filter(s -> request.getReference().equals(s.getReference()))
                .findFirst();
        Optional<SavingsWithdrawApplication> withdrawWithReference = savingsWithdrawApplicationRepo
                .findBySavingsId(request.getSavingsId());



        if(request.getStatus() == Status.WITHDRAW && withdrawWithReference.isPresent()){
            UserBank bank = userBankRepo.findBySavingsId(request.getSavingsId());
            SavingsWithdrawApplication withdraw = savingsWithdrawApplicationRepo.findBySavingsId(request.getSavingsId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "no record found in withdraw application"));
            List<UserSavings> savings = userSavingsRepo.findBySavingsId(request.getSavingsId());


            if (bank == null){
                return new ApiResponse<>(
                        false,
                        "Theres no Savings Id found in Bank",
                        null
                );
            }

            if (savings == null){
                return new ApiResponse<>(
                        false,
                        "Theres no Savings Id found in Bank",
                        null
                );
            }


            BigDecimal balance = withdraw.getAccountBalance().add(withdraw.getAnnual());

            Ledger withdrawHistory = new Ledger();
            withdrawHistory.setAmount(balance);
            withdrawHistory.setSavingsId(withdraw.getSavingsId());
            withdrawHistory.setDepositDate(withdraw.getWithdrawDate());
            withdrawHistory.setReference(withdraw.getReference());
            withdrawHistory.setSavingsId(withdraw.getSavingsId());
            withdrawHistory.setDescription(Description.Withdrawal);
            withdrawHistory.setCreatedAt(LocalDateTime.now());
            withdrawHistory.setModeOfPayment(PaymentMethod.CASH);
            withdrawHistory.setUserId(withdraw.getUserId());

            ledgerRepo.save(withdrawHistory);

            // savings ledger
//            List<Ledger> savingsLedger = savings.stream()
//                    .map(saving -> Ledger.builder()
//                            .savingsId(saving.getSavingsId())
//                            .userId(saving.getUserId())
//                            .amount(BigDecimal.valueOf(saving.getAmountDeposit()))
//                            .depositDate(saving.getDepositDate())
//                            .reference(saving.getReference())
//                            .description(Description.Savings)
//                            .modeOfPayment(saving.getPaymentMethod())
//                            .createdAt(LocalDateTime.now())
//                            .build())
//                    .toList();
//
//            ledgerRepo.saveAll(savingsLedger);

            savingsWithdrawApplicationRepo.delete(withdraw);
            userSavingsRepo.deleteAll(savings);

            bank.setAccountBalance(BigDecimal.ZERO);
            bank.setFirstDepositDate(null);
            bank.setHasSavingsDeposit(false);
            userBankRepo.save(bank);
            sseController.notifyUpdate();
            notificationService.notifyUserPaymentMade(bank.getUserId(), userBank.getSavingsId(), info.getFirstName(), bank.getAccountBalance());
            return new ApiResponse<>(true, "Withdrawal processed successfully", null);
        }

        if (request.getStatus() == Status.REJECTED) {
            Optional<SavingsWithdrawApplication> withdraw = savingsWithdrawApplicationRepo.findBySavingsId(request.getSavingsId());

            if (withdraw.isPresent()) {
                SavingsWithdrawApplication withdrawApplication = withdraw.get();
                savingsWithdrawApplicationRepo.delete(withdrawApplication);
                sseController.notifyUpdate();
                notificationService.notifyUserPaymentMade(withdrawApplication.getUserId(), userBank.getSavingsId(), info.getFirstName(), withdrawApplication.getAccountBalance());
                return new ApiResponse<>(true, "Withdraw Application Rejected", null);
            }
            userSavingsRepo.deleteBySavingsIdAndStatus(request.getSavingsId());
            return new ApiResponse<>(true, "Savings Payment Rejected", null);
        }


        if (savingsWithReference.isPresent()) {
            Status status = savingsWithReference.get().getStatus();
            UserSavings reference = savingsWithReference.get();


            if (request.getStatus() == status){
                return new ApiResponse<>(true, "Payment already processed", null);
            }

            if (request.getStatus() == Status.REJECTED) {
                sseController.notifyUpdate();
                return new ApiResponse<>(true, "Payment deleted", null);
            }

            if (userBank.getFirstDepositDate() == null) {
                userBank.setFirstDepositDate(reference.getDepositDate());
                userBank.setHasSavingsDeposit(true);

                userBankRepo.save(userBank);
            }

            BigDecimal addPayment = userBank.getAccountBalance().add(BigDecimal.valueOf(reference.getAmountDeposit()));

            Ledger ledgerEntry = Ledger.builder()
                    .userId(reference.getUserId())
                    .savingsId(reference.getSavingsId())
                    .amount(BigDecimal.valueOf(reference.getAmountDeposit()))
                    .depositDate(reference.getDepositDate())
                    .reference(reference.getReference())
                    .description(Description.Savings)
                    .modeOfPayment(reference.getPaymentMethod())
                    .createdAt(LocalDateTime.now())
                    .build();
            ledgerRepo.save(ledgerEntry);

            userBank.setAccountBalance(addPayment);
            userBankRepo.save(userBank);
            reference.setStatus(Status.PAID);
            userSavingsRepo.save(reference);
            notificationService.notifyUserPaymentMade(reference.getUserId(), userBank.getSavingsId(), info.getFirstName(), BigDecimal.valueOf(reference.getAmountDeposit()));
            sseController.notifyUpdate();
            return new ApiResponse<>(true, "Payment Added to User", null);

        }else {
            return new ApiResponse<>(false, "Missing information " + request.getSavingsId() +" or " + request.getReference(), null);
        }

    }


    public ApiResponse<UserDepositSavingsResponse>  userDeposit(Long userId, double depositAmount, LocalDate depositDate){
        UserBank user = userBankRepo.findByUserId(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "userId not found"));

        UserInfo info = userInfoRepo.findByUserId(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "userId not found"));

        ApiResponse<UserDepositSavingsResponse> x = checkUserDepositInput(userId, depositAmount, depositDate);
        if (x != null) return x;

        LocalDateTime depositDateTime = depositDate.atTime(LocalTime.now());

            UserSavings deposit;
                UserSavings userSavings = new UserSavings();
                userSavings.setUserId(userId);
                userSavings.setSavingsId(user.getSavingsId());
                userSavings.setAmountDeposit(depositAmount);
                userSavings.setDepositDate(depositDateTime);
                userSavings.setPaymentMethod(PaymentMethod.CASH);
                userSavings.setReference(generateRef());
                userSavings.setStatus(Status.PENDING);

                deposit = userSavingsRepo.save(userSavings);

                sseController.notifyUpdate();
                notificationService.notifyAdminPaymentMade( user.getSavingsId(), info.getFirstName(), depositAmount, userSavings.getReference());
            UserDepositSavingsResponse responseData = mapToUserSavingsResponse(deposit);

            return new ApiResponse<>(
                    true,
                    "Deposit created successfully",
                    responseData
            );


    }

    public AdminTallySavings getTallySavings(){

        BigDecimal totalDeposits = Optional.ofNullable(userSavingsRepo.sumAllDeposits())
                .orElse(BigDecimal.valueOf(0.0));

        int totalMembers = Optional.of(userRepo.countAllSavingsMembers()).orElse(0);
        int totalPending = Optional.of(userSavingsRepo.countAllPendingDeposits()).orElse(0);

        return AdminTallySavings.builder()
                .overAllSavings(totalDeposits)
                .totalMembers(totalMembers)
                .totalPending(Math.toIntExact(totalPending))
                .build();
    }

    public ApiResponse<?> getAllSavingsMembers(){
        return new ApiResponse<>(
                true,
                "Successful",
                userSavingsRepo.findAllMembers()
        );
    }

    public ApiResponse<?> getAllPendingPayments(String savingsId, int page, int size) {
        UserBank user = userBankRepo.findBySavingsId(savingsId);

        if (user == null) {
            return new ApiResponse<>(
                    false,
                    "Savings Id not exist in records",
                    null
            );
        }

        UserInfo userInfo = userInfoRepo.findByUserId(user.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "userid not found"));

        SavingsWithdrawApplication savingsWithdrawApplication =
                savingsWithdrawApplicationRepo.findByUserId(user.getUserId());

        WithdrawSavingsInfo withdraw = WithdrawSavingsInfo.builder().build();

        if (savingsWithdrawApplication != null) {
            BigDecimal balance = savingsWithdrawApplication.getAccountBalance()
                    .add(savingsWithdrawApplication.getAnnual());
            withdraw = WithdrawSavingsInfo.builder()
                    .withdrawDate(savingsWithdrawApplication.getWithdrawDate())
                    .reference(savingsWithdrawApplication.getReference())
                    .totalBalance(balance)
                    .status(savingsWithdrawApplication.getStatus())
                    .build();
        }

        UserSavingsInfo userSavingsInfo = UserSavingsInfo.builder()
                .firstName(userInfo.getFirstName())
                .lastName(userInfo.getLastName())
                .targetAmount(user.getTargetAmount())
                .savingsId(user.getSavingsId())
                .accountBalance(user.getAccountBalance())
                .maturityDate(user.getFirstDepositDate() != null
                        ? user.getFirstDepositDate().plusYears(1)
                        : null)
                .profileImage(userInfo.getProfileImage())
                .build();

        Pageable pageable = PageRequest.of(page, size);
        Page<SavingsPendingPaymentMemberResponse> paymentsPage =
                userBankRepo.findPendingPaymentBySavingsId(savingsId, pageable);

        return new ApiResponse<>(
                true,
                "Success",
                SavingsDetailResponse.builder()
                        .user(userSavingsInfo)
                        .payments(paymentsPage.getContent())
                        .currentPage(paymentsPage.getNumber())
                        .totalPages(paymentsPage.getTotalPages())
                        .totalElements(paymentsPage.getTotalElements())
                        .last(paymentsPage.isLast())
                        .withdraw(withdraw)
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

    public ApiResponse<SavingsSummaryResponse> savingsAllData(Long userId, int page, int size){
        List<UserSavings> userSavings = userSavingsRepo.findByUserId(userId);

        boolean withdraw = savingsWithdrawApplicationRepo.existsByUserId(userId);

        UserBank userBank = userBankRepo.findByUserId(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "userId is not found"));

         String savingsId = userBank.getSavingsId();
         BigDecimal targetAmount = Optional.ofNullable(userBank.getTargetAmount()).orElse(BigDecimal.ZERO);
         boolean isOneYear = userBank.getFirstDepositDate() != null
                 && ChronoUnit.YEARS.between(userBank.getFirstDepositDate(), LocalDateTime.now()) >= 1;
         BigDecimal userTargetAmount = targetAmount;

         Pageable pageable = PageRequest.of(page > 0 ? page - 1 : 0, size);
         Page<SavingsDepositHistory> savingsDepositHistoryList = userSavingsRepo
                 .findAllByUserIdAndStatusOrderByDepositDateDesc(userId, Status.PAID, pageable)
                 .map(this::allHistory);


         BigDecimal totalSavings = Optional.ofNullable(userSavingsRepo.sumAllPaidByUserId(userId))
                 .orElse(BigDecimal.ZERO);

         BigDecimal savingsBase = new BigDecimal("5000");
         BigDecimal annualBase = new BigDecimal("500");

         BigDecimal maxAnnual = targetAmount.compareTo(BigDecimal.ZERO) > 0 ? targetAmount
                 .divide(savingsBase, 10, RoundingMode.HALF_UP)
                 .multiply(annualBase)
                 .setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

         BigDecimal savingsForAnnual = targetAmount.compareTo(BigDecimal.ZERO) > 0 ? totalSavings
                 .min(targetAmount)
                 .setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

         BigDecimal userAnnual = targetAmount.compareTo(BigDecimal.ZERO) > 0 ? savingsForAnnual
                 .divide(savingsBase, 10, RoundingMode.HALF_UP)
                 .multiply(annualBase)
                 .min(maxAnnual)
                 .setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

         boolean isTargetReached = targetAmount.compareTo(BigDecimal.ZERO) > 0 && totalSavings.compareTo(targetAmount) >= 0;



        SavingsSummaryResponse response = new SavingsSummaryResponse();
        response.setSavingsId(savingsId);
        response.setTotalSavingsBalance(totalSavings);
        response.setTargetAmount(userTargetAmount);
        response.setDepositHistoryList(savingsDepositHistoryList.getContent());
        response.setCurrentPage(savingsDepositHistoryList.getNumber());
        response.setTotalPages(savingsDepositHistoryList.getTotalPages());
        response.setTotalElements(savingsDepositHistoryList.getTotalElements());
        response.setLast(savingsDepositHistoryList.isLast());
        response.setTargetReached(isTargetReached);
        response.setHasWithdraw(withdraw);
        response.setOneYear(isOneYear);
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

        sseController.notifyUpdate();

        return  new ApiResponse<>(
                true,
                "Successfully applied Savings",
                userBank
        );
    }

    private static ApiResponse<?> savingsValidation(BigDecimal targetAmount, UserInfo userInfo, UserBank userBank) {
        if (userInfo.getAddress() == null || userInfo.getBirthDay() == null|| userInfo.getFirstName() == null || userInfo.getGender() == null || userInfo.getLastName() == null || userInfo.getMiddleName() == null || userInfo.getPhoneNumber() == null || userInfo.getSuffix() == null || userInfo.getProfileImage() == null) {
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

        UserInfo userInfo = userInfoRepo.findByUserId(userId).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "User not found"));

        boolean hasApplication  = savingsWithdrawApplicationRepo
                .existsByUserIdAndStatus(userId,Status.WITHDRAW);

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

        notificationService.notifySavingsWithdraw(user.getSavingsId(),user.getSavingsId(),userInfo.getFirstName());

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
