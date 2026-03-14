package com.example.MobilePaluwagan.Service;

import com.example.MobilePaluwagan.Controller.SseController;
import com.example.MobilePaluwagan.DTOs.Request.PaymentAdminRequest;
import com.example.MobilePaluwagan.DTOs.Response.*;
import com.example.MobilePaluwagan.Entity.*;
import com.example.MobilePaluwagan.Repository.*;
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

    @Autowired
    private DueDateScheduleRepository dueDateScheduleRepository;

    @Autowired
    private UserLoanRepo userLoanRepo;

    @Autowired
    private LoanPaymentRepo loanPaymentRepo;

    @Autowired
    private SseController  sseController;

    @Autowired
    private UserInfoRepo userInfoRepo;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserBankRepo userBankRepo;

    @Autowired
    private UserSavingsRepo userSavingsRepo;

    @Autowired
    private LedgerRepo ledgerRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private DueDateScheduleRepository dueDateSchedule;

    @Autowired
    private LoanApplicationRepo loanApplicationRepo;

    @Autowired
    private UserLoanRepo loanUserLoanRepo;


    @Transactional
    public ApiResponse<?> processLoanPayment(PaymentAdminRequest request) {

        Loan user = userLoanRepo.findByApplicationID(Long.valueOf(request.getApplicationId()));

        UserInfo userInfo = userInfoRepo.findByUserId(user.getUserId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        checkForMaturityDateLoan(request.getApplicationId());
        if (user.getLoanRepaymentTally().compareTo(user.getTotalRepayable()) >= 0) {
            return new ApiResponse<>(false, "The loan is already paid", null);
        }

        DueDateSchedule current = dueDateScheduleRepository
                .findFirstPendingOrPartial(user.getApplicationID())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No pending payments found for loan"));

        BigDecimal requireAmount = current.getPayment();
        BigDecimal amountPaid = BigDecimal.valueOf(request.getAmount());

        if (amountPaid.compareTo(requireAmount) < 0) {

            BigDecimal shortage = requireAmount.subtract(amountPaid)
                    .setScale(2, RoundingMode.HALF_UP);

            current.setStatus(Status.PARTIAL);
            current.setPayment(shortage);
            dueDateScheduleRepository.save(current);

            Optional<DueDateSchedule> nextWeekOpt = dueDateScheduleRepository
                    .findFirstByApplicationIdAndStatusOrderByDueDateAsc(
                            current.getApplicationId(), Status.PENDING);

            if (nextWeekOpt.isPresent()) {

                DueDateSchedule nextWeek = nextWeekOpt.get();
                nextWeek.setPayment(nextWeek.getPayment()
                        .add(shortage)
                        .setScale(2, RoundingMode.HALF_UP));
                dueDateScheduleRepository.save(nextWeek);
            }

        } else if (amountPaid.compareTo(requireAmount) == 0) {

            long remainingCount = dueDateScheduleRepository
                    .countPendingOrPartial(current.getApplicationId());

            if (remainingCount == 1) {
                current.setRemainingBalance(BigDecimal.ZERO);
            }

            current.setStatus(Status.PAID);
            dueDateScheduleRepository.save(current);

        } else {

            long remainingCount = dueDateScheduleRepository
                    .countPendingOrPartial(current.getApplicationId());

            if (remainingCount == 1) {
                current.setRemainingBalance(BigDecimal.ZERO);
            }

            current.setStatus(Status.PAID);
            dueDateScheduleRepository.save(current);

            BigDecimal excess = amountPaid.subtract(requireAmount)
                    .setScale(2, RoundingMode.HALF_UP);

            int weeksCovered = 0;

            List<DueDateSchedule> futureWeeks = dueDateScheduleRepository
                    .findByApplicationIdAndStatus(current.getApplicationId(), Status.PENDING);

            for (DueDateSchedule futureWeek : futureWeeks) {

                if (excess.compareTo(BigDecimal.ZERO) <= 0) break;

                BigDecimal futurePayment = futureWeek.getPayment();

                if (excess.compareTo(futurePayment) >= 0) {
                    excess = excess.subtract(futurePayment)
                            .setScale(2, RoundingMode.HALF_UP);
                    futureWeek.setStatus(Status.PAID);
                    dueDateScheduleRepository.save(futureWeek);
                    weeksCovered++;
                } else {
                    BigDecimal reducePayment = futureWeek.getPayment()
                            .subtract(excess)
                            .setScale(2, RoundingMode.HALF_UP);
                    BigDecimal reduceBalance = futureWeek.getRemainingBalance()
                            .subtract(excess)
                            .setScale(2, RoundingMode.HALF_UP);
                    futureWeek.setPayment(reducePayment);
                    futureWeek.setRemainingBalance(reduceBalance);
                    dueDateScheduleRepository.save(futureWeek);
                    excess = BigDecimal.ZERO;
                    break;
                }
            }
        }

        LoanPayment transaction = new LoanPayment();
        transaction.setLoanId(user.getId());
        transaction.setUserId(user.getUserId());
        transaction.setAmountPaid(amountPaid);
        transaction.setPaymentDate(LocalDateTime.now());
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setReferenceNumber(generateRef());
        String bankRef = request.getBankReference();
        transaction.setBankReference(
                (bankRef == null || bankRef.trim().isEmpty()) ? null : bankRef
        );
        transaction.setStatus(Status.PAID);
        loanPaymentRepo.save(transaction);

        user.setLoanRepaymentTally(user.getLoanRepaymentTally().add(amountPaid));
        userLoanRepo.save(user);
        notificationService.notifyUserPaymentMade(user.getUserId(), String.valueOf(request.getApplicationId()), userInfo.getFirstName(), BigDecimal.valueOf(request.getAmount()));
        sseController.notifyUpdate();

        return new ApiResponse<>(true, "Payment processed successfully.", null);
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

            List<Ledger> loanLedger = payment.stream()
                    .map(payments -> Ledger.builder()
                            .savingsId(String.valueOf(userLoan.getApplicationID()))
                            .userId(payments.getUserId())
                            .amount(payments.getAmountPaid())
                            .depositDate(payments.getPaymentDate())
                            .reference(payments.getReferenceNumber())
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
           savings.setReference(generateRef());
           String bankRef = request.getBankReference();
           savings.setBankReference(
                   (bankRef == null || bankRef.trim().isEmpty()) ? null : bankRef
           );
           savings.setStatus(Status.PAID);

           userSavingsRepo.save(savings);


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


    public String generateRef() {
        String prefix = "REF";
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();

        String letter1 = String.valueOf(randomLetters.charAt(random.nextInt(26)));
        String letter2 = String.valueOf(randomLetters.charAt(random.nextInt(26)));
        String letter3 = String.valueOf(randomLetters.charAt(random.nextInt(26)));

        Optional<LoanPayment> lastRef = loanPaymentRepo.findLastRef();

        int sequence = 1;

        if(lastRef.isPresent()){
            String lastRefNumber = lastRef.get().getReferenceNumber();

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
