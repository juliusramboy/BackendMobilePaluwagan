package com.example.MobilePaluwagan.Service;

import com.example.MobilePaluwagan.DTOs.Response.*;
import com.example.MobilePaluwagan.Entity.*;
import com.example.MobilePaluwagan.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class LoanService {

    @Autowired
    private UserBankRepo userBankRepo;

    @Autowired
    private UserInfoRepo userInfoRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private UserLoanRepo userLoanRepo;

    @Autowired
    private LoanApplicationRepo loanApplicationRepo;

    @Autowired
    private LoanPaymentRepo loanPaymentRepo;

    

    public UserApplyLoanResponse loanApplication(Long userId, BigDecimal requestedAmount, Integer termLength){
        Long applicationNumber = generateApplicationId(userId);

        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setApplicationID(applicationNumber);
        loanApplication.setUserId(userId);
        loanApplication.setRequestedAmount(requestedAmount);
        loanApplication.setTermLength(termLength);
        loanApplication.setApplicationDate(LocalDate.now());
        loanApplication.setStatus(Status.PENDING);

        LoanApplication saved = loanApplicationRepo.save(loanApplication);

        return mapToUserLoanResponse(saved);
    }

    private UserApplyLoanResponse mapToUserLoanResponse(LoanApplication loan) {
        return UserApplyLoanResponse.builder()
                .applicationNumber(loan.getApplicationID())
                .loanAmount(loan.getRequestedAmount())
                .termLength(loan.getTermLength())
                .status(loan.getStatus().name())
                .applicationDate(loan.getApplicationDate())
                .build();
    }

    public UserAllLoansResponse getAllTheInfo(Long userId){
        Optional<UserInfo> userInfo = userInfoRepo.findByUserId(userId);
        List<LoanApplication> applications = loanApplicationRepo.findAllByUserId(userId);
        List<Loan> loans = userLoanRepo.findAllByUserId(userId);
        List<LoanPayment> payments = loanPaymentRepo.findAllByUserId(userId);

        if (applications.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User has no loan records");
        }


        double totalPaid = payments.stream()
                .mapToDouble(LoanPayment::getAmountPaid)
                .sum();

        double totalLoanAmount = loans.stream()
                .mapToDouble(Loan::getAmount)
                .sum();

        double remainingBalance = 0.0;
        String progressMessage = "No active loan balance";

        if (totalLoanAmount > 0) {
            remainingBalance = totalLoanAmount - totalPaid;

            if (remainingBalance < 0) remainingBalance = 0;

            double percentPaid = (totalPaid / totalLoanAmount) * 100;

            progressMessage = String.format("%.0f", percentPaid);
        }

        List<LoanApplicationInfo> applicationInfos = applications.stream()
                .filter(app -> "APPROVED".equalsIgnoreCase(app.getStatus().name()))
                .map(app -> LoanApplicationInfo.builder()
                        .applicationNumber(app.getApplicationID())
                        .loanAmount(app.getRequestedAmount())
                        .termLength(app.getTermLength())
                        .status(app.getStatus().name())
                        .applicationDate(app.getApplicationDate())
                        .status(String.valueOf(app.getStatus()))
                        .build())
                .toList();

        List<LoanInfo> loanInfos = loans.stream()
                .map(loan -> LoanInfo.builder()
                        .loanId(loan.getId())
                        .loanAmount(BigDecimal.valueOf(loan.getAmount()))
                        .interestRate(BigDecimal.valueOf(loan.getInterestRate()))
                        .startDate(loan.getStartDate())
                        .build())
                .toList();

        List<PaymentInfo> paymentInfos = payments.stream()
                .map(payment -> PaymentInfo.builder()
                        .paymentId(payment.getId())
                        .referenceNumber(payment.getReferenceNumber())
                        .amountPaid(BigDecimal.valueOf(payment.getAmountPaid()))
                        .paymentDate(payment.getPaymentDate())
                        .paymentMethod(payment.getPaymentMethod().name())
                        .build())
                .toList();

        // Return wrapper with all lists
        return UserAllLoansResponse.builder()
                .applications(applicationInfos)
                .loans(loanInfos)
                .payments(paymentInfos)
                .totalAmountPaid(BigDecimal.valueOf(totalPaid))
                .paymentProgress(progressMessage)
                .remainingBalance(remainingBalance)
                .userName(userInfo.get().getFirstName())
                .build();
    }

    public String generateReferenceNumber(Long userId) {
        String prefix = "REFF";
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long sequence = userId; // or use a counter from DB
        return prefix + "-" + datePart + "-" + sequence;
    }

    public long generateApplicationId(Long userId) {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // Concatenate date + userId into one string
        String numericId = datePart + userId;
        // Convert to long
        return Long.parseLong(numericId);
    }


}
