package com.example.MobilePaluwagan.Service;

import com.example.MobilePaluwagan.Controller.SseController;
import com.example.MobilePaluwagan.DTOs.Request.PaymentLoanRequest;
import com.example.MobilePaluwagan.DTOs.Request.WeeklyAmortizationSchedule;
import com.example.MobilePaluwagan.DTOs.Response.AdminPaymentLoanSearchResponse;
import com.example.MobilePaluwagan.DTOs.Response.AdminPaymentSavingsSearchResponse;
import com.example.MobilePaluwagan.DTOs.Response.ApiResponse;
import com.example.MobilePaluwagan.Entity.*;
import com.example.MobilePaluwagan.Repository.*;
import com.google.common.math.Quantiles;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.ResourceTransactionManager;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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




    @Transactional
    public ApiResponse<?> processPayment(PaymentLoanRequest request) {

        Loan user = userLoanRepo.findByApplicationID(request.getApplicationId());

        UserInfo userInfo = userInfoRepo.findByUserId(user.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (user.getLoanRepaymentTally().compareTo(user.getTotalRepayable()) >= 0) {
            return new ApiResponse<>(false, "The loan is already paid", null);
        }

        DueDateSchedule current = dueDateScheduleRepository
                .findFirstPendingOrPartial(user.getApplicationID())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No pending payments found for loan"));

        BigDecimal requireAmount = current.getPayment();
        BigDecimal amountPaid = request.getAmount();

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
        transaction.setPaymentDate(LocalDate.now());
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setReferenceNumber(generateRef());
        transaction.setBankReference(request.getBankReference());
        transaction.setStatus(Status.PAID);
        loanPaymentRepo.save(transaction);

        user.setLoanRepaymentTally(user.getLoanRepaymentTally().add(amountPaid));
        userLoanRepo.save(user);
        notificationService.notifyUserPaymentMade(user.getId(), String.valueOf(request.getApplicationId()), userInfo.getFirstName(), request.getAmount());
        sseController.notifyUpdate();

        return new ApiResponse<>(true, "Payment processed successfully.", null);
    }

    public List<AdminPaymentLoanSearchResponse> searchLoanApplicant(String name){
        return loanPaymentRepo.searchApplicantLoanByName(name);
    }

    public List<AdminPaymentSavingsSearchResponse> searchSavingsApplicant(String name){
        return loanPaymentRepo.searchApplicantSavingsByName(name);
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
