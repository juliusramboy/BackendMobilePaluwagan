package com.example.MobilePaluwagan.service;

import com.example.MobilePaluwagan.controller.SseController;
import com.example.MobilePaluwagan.dto.Request.PaymentAdminRequest;
import com.example.MobilePaluwagan.dto.Response.*;
import com.example.MobilePaluwagan.entity.*;
import com.example.MobilePaluwagan.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PaymentService {


    private final DueDateScheduleRepository dueDateScheduleRepository;
    private final UserLoanRepo userLoanRepo;
    private final LoanPaymentRepo loanPaymentRepo;
    private final SseController  sseController;
    private final UserInfoRepo userInfoRepo;
    private final NotificationService notificationService;
    private final UserBankRepo userBankRepo;
    private final UserSavingsRepo userSavingsRepo;
    private final LedgerRepo ledgerRepo;
    private final UserRepo userRepo;
    private final DueDateScheduleRepository dueDateSchedule;
    private final LoanApplicationRepo loanApplicationRepo;
    private final UserLoanRepo loanUserLoanRepo;

    // original logic for payment
//    @Transactional
//    public ApiResponse<?> processLoanPayment(PaymentAdminRequest request) {
//
//        Loan user = userLoanRepo.findByApplicationID(Long.valueOf(request.getApplicationId()));
//        UserInfo userInfo = userInfoRepo.findByUserId(user.getUserId())
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
//
//        checkForMaturityDateLoan(request.getApplicationId());
//
//        if (user.getLoanRepaymentTally().compareTo(user.getTotalRepayable()) >= 0) {
//            return new ApiResponse<>(false, "The loan is already paid", null);
//        }
//
//        BigDecimal amountPaid = BigDecimal.valueOf(request.getAmount());
//        BigDecimal remaining = amountPaid;
//
//        while (remaining.compareTo(BigDecimal.ZERO) > 0) {
//
//            Optional<DueDateSchedule> currentOpt = dueDateScheduleRepository
//                    .findFirstPendingOrPartial(user.getApplicationID());
//
//            if (currentOpt.isEmpty()) break;
//
//            DueDateSchedule current = currentOpt.get();
//            BigDecimal requireAmount = current.getPayment();
//
//            //  Safety check
//            if (requireAmount.compareTo(BigDecimal.ZERO) <= 0) break;
//
//            if (remaining.compareTo(requireAmount) < 0) {
//                // Not enough → PARTIAL
//                BigDecimal shortage = requireAmount.subtract(remaining)
//                        .setScale(2, RoundingMode.HALF_UP);
//
//                current.setStatus(Status.PARTIAL);
//                current.setPayment(shortage);
//                current.setRemainingBalance(shortage); //  track remaining balance
//                dueDateScheduleRepository.save(current);
//                remaining = BigDecimal.ZERO;
//
//            } else if (remaining.compareTo(requireAmount) == 0) {
//                // Exactly enough → PAID
//                long remainingCount = dueDateScheduleRepository
//                        .countPendingOrPartial(current.getApplicationId());
//                if (remainingCount == 1) {
//                    current.setRemainingBalance(BigDecimal.ZERO);
//                }
//                current.setStatus(Status.PAID);
//                dueDateScheduleRepository.save(current);
//                remaining = BigDecimal.ZERO;
//
//            } else {
//                // More than enough → PAID + continue loop
//                long remainingCount = dueDateScheduleRepository
//                        .countPendingOrPartial(current.getApplicationId());
//                if (remainingCount == 1) {
//                    current.setRemainingBalance(BigDecimal.ZERO);
//                }
//                current.setStatus(Status.PAID);
//                dueDateScheduleRepository.save(current);
//                remaining = remaining.subtract(requireAmount)
//                        .setScale(2, RoundingMode.HALF_UP);
//            }
//        }
//
//        // Save LoanPayment transaction
//        LoanPayment transaction = new LoanPayment();
//        transaction.setLoanId(user.getId());
//        transaction.setUserId(user.getUserId());
//        transaction.setAmountPaid(amountPaid);
//        transaction.setPaymentDate(LocalDateTime.now());
//        transaction.setPaymentMethod(request.getPaymentMethod());
//        transaction.setReferenceNumber(generateRef());
//        transaction.setPaymentMethod(PaymentMethod.CASH);
//        String bankRef = request.getBankReference();
//        transaction.setBankReference(
//                (bankRef == null || bankRef.trim().isEmpty()) ? null : bankRef
//        );
//        transaction.setStatus(Status.PAID);
//        loanPaymentRepo.save(transaction);
//
//        user.setLoanRepaymentTally(user.getLoanRepaymentTally().add(amountPaid));
//        userLoanRepo.save(user);
//
//        notificationService.notifyUserPaymentMade(
//                user.getUserId(),
//                String.valueOf(request.getApplicationId()),
//                userInfo.getFirstName(),
//                BigDecimal.valueOf(request.getAmount())
//        );
//        sseController.notifyUpdate();
//        checkForMaturityDateLoan(request.getApplicationId());
//        return new ApiResponse<>(true, "Payment processed successfully.", null);
//    }


    @Transactional
    public ApiResponse<?> processLoanPayment(PaymentAdminRequest request) {

        Loan user = userLoanRepo.findByApplicationID(Long.valueOf(request.getApplicationId()));
        UserInfo userInfo = userInfoRepo.findByUserId(user.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        checkForMaturityDateLoan(request.getApplicationId());

        // Check 1 — fully paid na
        if (user.getLoanRepaymentTally().compareTo(user.getTotalRepayable()) >= 0) {
            return new ApiResponse<>(false, "The loan is already paid", null);
        }

        BigDecimal amountPaid = BigDecimal.valueOf(request.getAmount());
        BigDecimal remainingBalance = user.getTotalRepayable()
                .subtract(user.getLoanRepaymentTally())
                .setScale(2, RoundingMode.HALF_UP);

        // Check 2 — sobra ng bayad (hanggang 5 lang pede)
        BigDecimal tolerance = new BigDecimal("5.00");
        BigDecimal overpayment = amountPaid.subtract(remainingBalance);

        if (overpayment.compareTo(tolerance) > 0) {
            return new ApiResponse<>(
                    false,
                    "Payment exceeds the remaining balance by ₱" + overpayment.setScale(2, RoundingMode.HALF_UP)
                            + ". Remaining balance is ₱" + remainingBalance + ". Maximum allowed overpayment is ₱5.00.",
                    null
            );
        }

        //  Save sa ledger
        LoanPayment transaction = new LoanPayment();
        transaction.setLoanId(user.getId());
        transaction.setUserId(user.getUserId());
        transaction.setAmountPaid(amountPaid);
        transaction.setPaymentDate(LocalDateTime.now());
        transaction.setPaymentMethod(request.getPaymentMethod());
        String ref;
        do {
            ref = com.example.MobilePaluwagan.util.IdGenerator.generateShortCode("LP-", 8);
        } while (loanPaymentRepo.existsByReferenceNumber(ref));
        transaction.setReferenceNumber(ref);
        String bankRef = request.getBankReference();
        transaction.setBankReference(
                (bankRef == null || bankRef.trim().isEmpty()) ? null : bankRef
        );
        transaction.setStatus(Status.PAID);
        loanPaymentRepo.save(transaction);

        // save to db every transaction (loan)
        Ledger ledger = new Ledger();
        ledger.setUserId(user.getUserId());
        ledger.setSavingsId(request.getApplicationId());
        ledger.setAmount(BigDecimal.valueOf(request.getAmount()));
        ledger.setDepositDate(LocalDateTime.now());
        ledger.setReference(transaction.getReferenceNumber());
        ledger.setCreatedAt(LocalDateTime.now());
        ledger.setDescription(Description.Loan);
        ledger.setModeOfPayment(request.getPaymentMethod()); // this will get the payment from front
        ledgerRepo.save(ledger);

        //  Update tally
        user.setLoanRepaymentTally(user.getLoanRepaymentTally().add(amountPaid));
        userLoanRepo.save(user);

        //  Auto-call — update DueDateSchedule statuses
        updateDueDateSchedule(user.getApplicationID(), amountPaid);

        notificationService.notifyUserPaymentMade(
                user.getUserId(),
                String.valueOf(request.getApplicationId()),
                userInfo.getFirstName(),
                BigDecimal.valueOf(request.getAmount())
        );
        sseController.notifyUpdate();
        checkForMaturityDateLoan(request.getApplicationId());

        return new ApiResponse<>(true, "Payment processed successfully.", null);
    }

    //  Hiwalay na method — waterfall logic nandito na lang
    private void updateDueDateSchedule(Long applicationId, BigDecimal amountPaid) {
        BigDecimal remaining = amountPaid;

        while (remaining.compareTo(BigDecimal.ZERO) > 0) {

            Optional<DueDateSchedule> currentOpt = dueDateScheduleRepository
                    .findFirstPendingOrPartial(applicationId);

            if (currentOpt.isEmpty()) break;

            DueDateSchedule current = currentOpt.get();
            BigDecimal requiredAmount = current.getPayment();

            if (requiredAmount.compareTo(BigDecimal.ZERO) <= 0) break;

            if (remaining.compareTo(requiredAmount) < 0) {
                // PARTIAL
                BigDecimal shortage = requiredAmount.subtract(remaining)
                        .setScale(2, RoundingMode.HALF_UP);
                current.setStatus(Status.PARTIAL);
                current.setPayment(shortage);
                current.setRemainingBalance(shortage);
                dueDateScheduleRepository.save(current);
                remaining = BigDecimal.ZERO;

            } else if (remaining.compareTo(requiredAmount) == 0) {
                // EXACTLY PAID
                long remainingCount = dueDateScheduleRepository
                        .countPendingOrPartial(applicationId);
                if (remainingCount == 1) current.setRemainingBalance(BigDecimal.ZERO);
                current.setStatus(Status.PAID);
                dueDateScheduleRepository.save(current);
                remaining = BigDecimal.ZERO;

            } else {
                // MORE THAN ENOUGH — continue loop
                long remainingCount = dueDateScheduleRepository
                        .countPendingOrPartial(applicationId);
                if (remainingCount == 1) current.setRemainingBalance(BigDecimal.ZERO);
                current.setStatus(Status.PAID);
                dueDateScheduleRepository.save(current);
                remaining = remaining.subtract(requiredAmount)
                        .setScale(2, RoundingMode.HALF_UP);
            }
        }
    }
    @Transactional
    public void processLoanLogic(String applicationId, BigDecimal amountPaid,
                                 PaymentMethod paymentMethod, String bankReference) {

        Loan user = userLoanRepo.findByApplicationID(Long.valueOf(applicationId));

        UserInfo userInfo = userInfoRepo.findByUserId(user.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        checkForMaturityDateLoan(String.valueOf(user.getApplicationID()));

        if (user.getLoanRepaymentTally().compareTo(user.getTotalRepayable()) >= 0) {
            throw new RuntimeException("The loan is already paid");
        }

        BigDecimal remaining = amountPaid;

        while (remaining.compareTo(BigDecimal.ZERO) > 0) {

            Optional<DueDateSchedule> currentOpt = dueDateScheduleRepository
                    .findFirstPendingOrPartial(user.getApplicationID());

            if (currentOpt.isEmpty()) break;

            DueDateSchedule current = currentOpt.get();
            BigDecimal requireAmount = current.getPayment();

            //  Safety check
            if (requireAmount.compareTo(BigDecimal.ZERO) <= 0) break;

            if (remaining.compareTo(requireAmount) < 0) {
                // Not enough → PARTIAL
                BigDecimal shortage = requireAmount.subtract(remaining)
                        .setScale(2, RoundingMode.HALF_UP);

                current.setStatus(Status.PARTIAL);
                current.setPayment(shortage);
                current.setRemainingBalance(shortage);
                dueDateScheduleRepository.save(current);
                remaining = BigDecimal.ZERO;

            } else if (remaining.compareTo(requireAmount) == 0) {
                // Exactly enough → PAID
                long remainingCount = dueDateScheduleRepository
                        .countPendingOrPartial(current.getApplicationId());
                if (remainingCount == 1) {
                    current.setRemainingBalance(BigDecimal.ZERO);
                }
                current.setStatus(Status.PAID);
                dueDateScheduleRepository.save(current);
                remaining = BigDecimal.ZERO;

            } else {
                // More than enough → PAID + continue loop
                long remainingCount = dueDateScheduleRepository
                        .countPendingOrPartial(current.getApplicationId());
                if (remainingCount == 1) {
                    current.setRemainingBalance(BigDecimal.ZERO);
                }
                current.setStatus(Status.PAID);
                dueDateScheduleRepository.save(current);
                remaining = remaining.subtract(requireAmount)
                        .setScale(2, RoundingMode.HALF_UP);
            }
        }

        // Save LoanPayment
        LoanPayment transaction = new LoanPayment();
        transaction.setLoanId(user.getId());
        transaction.setUserId(user.getUserId());
        transaction.setAmountPaid(amountPaid);
        transaction.setPaymentDate(LocalDateTime.now());
        transaction.setPaymentMethod(paymentMethod);
        String ref;
        do {
            ref = com.example.MobilePaluwagan.util.IdGenerator.generateShortCode("LP-", 8);
        } while (loanPaymentRepo.existsByReferenceNumber(ref));
        transaction.setReferenceNumber(ref);
        transaction.setBankReference(bankReference);
        transaction.setStatus(Status.PAID);
        loanPaymentRepo.save(transaction);

        // Update loan tally
        user.setLoanRepaymentTally(user.getLoanRepaymentTally().add(amountPaid));
        userLoanRepo.save(user);

        // Notify user
        notificationService.notifyUserPaymentMade(
                user.getUserId(),
                String.valueOf(user.getApplicationID()),
                userInfo.getFirstName(),
                amountPaid
        );
        checkForMaturityDateLoan(String.valueOf(user.getApplicationID()));
        sseController.notifyUpdate();
    }

    private void checkForMaturityDateLoan(String applicationId) {
        Loan userLoan = userLoanRepo.findByApplicationID(Long.valueOf(applicationId));

        if (userLoan == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application Id Not Found");
        }

        List<LoanPayment> payment = loanPaymentRepo.findByLoanId(userLoan.getId());

        int tally = userLoan.getTotalRepayable().compareTo(userLoan.getLoanRepaymentTally());

        if(tally <= 0){
            User user = userRepo.findById(userLoan.getUserId()).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            List<DueDateSchedule> schedule = dueDateSchedule.findByApplicationId(Long.valueOf(applicationId));
            LoanApplication application = loanApplicationRepo.findByApplicationID(Long.valueOf(applicationId)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application Id Not Found"));

            Ledger loan = new Ledger();
            loan.setAmount(userLoan.getLoanRepaymentTally());
            loan.setDepositDate(LocalDateTime.now());
            loan.setReference(com.example.MobilePaluwagan.util.IdGenerator.generateShortCode("LC-", 8));
            loan.setSavingsId(String.valueOf(userLoan.getApplicationID()));
            loan.setUserId(userLoan.getUserId());
            loan.setCreatedAt(LocalDateTime.now());
            loan.setDescription(Description.Completed);


            ledgerRepo.save(loan);

            List<Ledger> loanLedger = payment.stream()
                    .map(payments -> Ledger.builder()
                            .savingsId(String.valueOf(userLoan.getApplicationID()))
                            .userId(payments.getUserId())
                            .amount(payments.getAmountPaid())
                            .depositDate(payments.getPaymentDate())
                            .reference(payments.getReferenceNumber())
                            .description(Description.Loan)
                            .modeOfPayment(payments.getPaymentMethod())
                            .createdAt(LocalDateTime.now())
                            .build())
                    .toList();

            ledgerRepo.saveAll(loanLedger);
            user.setHasLoan(false);
            userRepo.save(user);

            dueDateSchedule.deleteAll(schedule);
            loanPaymentRepo.deleteAll(payment);
            loanUserLoanRepo.delete(userLoan);
            loanApplicationRepo.delete(application);
        }

    }

    @Transactional
    public void processSavingsOnlinePayment(String savingsId, BigDecimal amountPaid,
                                    PaymentMethod paymentMethod, String bankReference) {


        // Find the savings account
        UserBank userbank = userBankRepo.findBySavingsId(String.valueOf(savingsId));

        if (userbank == null) {
            throw new RuntimeException("Savings account not found!");
        }

        if (!userbank.isHasSavingsDeposit()){
            userbank.setFirstDepositDate(LocalDateTime.now());
            userbank.setHasSavingsDeposit(true);
            userBankRepo.save(userbank);
        }

        // Add amount to balance
        BigDecimal newBalance = userbank.getAccountBalance().add(amountPaid);
        userbank.setAccountBalance(newBalance);
        userBankRepo.save(userbank);

        // Save savings transaction
        UserSavings savings = new UserSavings();
        savings.setSavingsId(String.valueOf(savingsId));
        savings.setDepositDate(LocalDateTime.now());
        savings.setAmountDeposit(amountPaid.doubleValue());
        savings.setUserId(userbank.getUserId());
        String ref;
        do {
            ref = com.example.MobilePaluwagan.util.IdGenerator.generateShortCode("SP-", 8);
        } while (userSavingsRepo.existsByReference(ref));
        savings.setReference(ref);
        savings.setPaymentMethod(paymentMethod);
        savings.setBankReference(
                (bankReference == null || bankReference.trim().isEmpty()) ? null : bankReference
        );
        savings.setStatus(Status.PAID);
        userSavingsRepo.save(savings);

        // save to db every transaction (savings)
        Ledger ledger = new Ledger();
        ledger.setUserId(userbank.getUserId());
        ledger.setSavingsId(savingsId);
        ledger.setAmount(amountPaid);
        ledger.setDepositDate(LocalDateTime.now());
        ledger.setReference(savings.getReference());
        ledger.setCreatedAt(LocalDateTime.now());
        ledger.setDescription(Description.Savings);
        ledger.setModeOfPayment(paymentMethod); // this will get the payment from the enum
        ledgerRepo.save(ledger);

        System.out.println("Savings payment processed successfully");
        sseController.notifyUpdate();
    }

    public ApiResponse<?> processSavingsPayment(PaymentAdminRequest request){
       UserBank userbank =  userBankRepo.findBySavingsId(request.getApplicationId());

       if(userbank != null){
           BigDecimal addPaymentAmount = userbank.getAccountBalance().add(BigDecimal.valueOf(request.getAmount()));
           userbank.setAccountBalance(addPaymentAmount);
           userBankRepo.save(userbank);


           UserSavings savings = new UserSavings();
           savings.setSavingsId(request.getApplicationId());
           savings.setDepositDate(LocalDateTime.now());
           savings.setAmountDeposit(request.getAmount());
           savings.setUserId(userbank.getUserId());
           String ref;
           do {
               ref = com.example.MobilePaluwagan.util.IdGenerator.generateShortCode("SP-", 8);
           } while (userSavingsRepo.existsByReference(ref));
           savings.setReference(ref);
           savings.setPaymentMethod(request.getPaymentMethod());
           String bankRef = request.getBankReference();
           savings.setBankReference(
                   (bankRef == null || bankRef.trim().isEmpty()) ? null : bankRef
           );
           savings.setStatus(Status.PAID);

           userSavingsRepo.save(savings);


           // save to db every transaction (savings)
           Ledger ledger = new Ledger();
           ledger.setUserId(userbank.getUserId());
           ledger.setSavingsId(request.getApplicationId());
           ledger.setAmount(BigDecimal.valueOf(request.getAmount()));
           ledger.setDepositDate(LocalDateTime.now());
           ledger.setReference(savings.getReference());
           ledger.setCreatedAt(LocalDateTime.now());
           ledger.setDescription(Description.Savings);
           ledger.setModeOfPayment(request.getPaymentMethod()); // this will get the payment from front
           ledgerRepo.save(ledger);

           System.out.println("Savings payment processed successfully");
           sseController.notifyUpdate();

           return new ApiResponse<>(true, "Savings payment processed successfully.", null);
       }else{
           return new ApiResponse<>(false, "Savings not found", null);
       }

    }






    public LoanApplicationResponseAdmin searchLoanApplicant(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<AdminPaymentLoanSearchResponse> result;

        if (name == null) {
            result = loanPaymentRepo.findAllLoanApplicants(pageable);
        }else{
            result = loanPaymentRepo.searchApplicantLoanByName(name, pageable);
        }


        return LoanApplicationResponseAdmin.builder()
                .success(true)
                .message(name == null?"All Applicants" : "Search results for: " + name)
                .paymentLoans(result.getContent())
                .currentPage(result.getNumber())
                .totalPages(result.getTotalPages())
                .totalElements(result.getTotalElements())
                .last(result.isLast())
                .build();
    }

    public SavingsApplicationResponseAdmin searchSavingsApplicant(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<AdminPaymentSavingsSearchResponse> result;

        if (name == null) {
            result = userBankRepo.findAllSavingsMembers(pageable);
        }else{
            result = userBankRepo.searchApplicantSavingsByName(name, pageable);
        }

        return SavingsApplicationResponseAdmin.builder()
                .success(true)
                .message("Results for: " + name)
                .paymentSavings(result.getContent())
                .totalPages(result.getTotalPages())
                .totalElements(result.getTotalElements())
                .last(result.isLast())
                .build();
    }



}
