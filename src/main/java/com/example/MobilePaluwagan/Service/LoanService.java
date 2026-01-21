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



    public ApiResponse<UserApplyLoanResponse> loanApplication(Long userId, BigDecimal requestedAmount, LocalDate termLength, LocalDate startDate){
        Optional<User> checkIfHaveLoan = userRepo.findById(userId);
        Optional<Loan> checkIfHavePassLoan = userLoanRepo.findByUserId(userId);
        Optional<LoanApplication> checkIfUserHaveAppllication = loanApplicationRepo.findByUserId(userId);


        if(checkIfHaveLoan.isPresent() && checkIfHaveLoan.get().isHasLoan()){
            return new ApiResponse<>(
                    false,
                    "We see that you have a pending Loan. Pay all your balance to make another loan.",
                    null
            );
        }

        boolean hasPendingApp = checkIfUserHaveAppllication.stream()
                .anyMatch(app -> "PENDING".equalsIgnoreCase(app.getStatus().name()));

        if (hasPendingApp) {
            return new ApiResponse<>(
                    false,
                    "You already have a pending loan application. Please wait for approval.",
                    null
            );
        }

        if(checkIfHavePassLoan.isPresent()){
            return new ApiResponse<>(
                    false,
                    "You already have a already loan. Please pay you all pending balance to loan again.",
                    null
            );
        }

        Long applicationNumber = generateApplicationId(userId);

        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setApplicationID(applicationNumber);
        loanApplication.setUserId(userId);
        loanApplication.setRequestedAmount(requestedAmount);
        loanApplication.setTermLength(termLength);
        loanApplication.setApplicationDate(startDate);
        loanApplication.setStatus(Status.PENDING);

        LoanApplication saved = loanApplicationRepo.save(loanApplication);

        UserApplyLoanResponse applyLoan = mapToUserLoanResponse(saved);

        return new ApiResponse<>(
                true,
                "Successfully Applied loan pls wait for admin to verify it",
                applyLoan
        );
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

    public ApiResponse<UserAllLoansResponse> getAllTheInfo(Long userId){
        Optional<UserInfo> userInfo = userInfoRepo.findByUserId(userId);
        List<LoanApplication> applications = loanApplicationRepo.findAllByUserId(userId);
        List<Loan> loans = userLoanRepo.findAllByUserId(userId);
        List<LoanPayment> payments = loanPaymentRepo.findAllByUserId(userId);

        if (applications.isEmpty()) {
            UserAllLoansResponse response = UserAllLoansResponse.builder()
                    .applications(List.of())
                    .loans(List.of())
                    .payments(List.of())
                    .totalAmountPaid(BigDecimal.ZERO)
                    .paymentProgress("")
                    .remainingBalance(0.0)
                    .userName(userInfo.get().getFirstName())
                    .eligible(true)
                    .build();

            return new ApiResponse<>(true, "User is eligible to make loan", response);
        }


        double totalPaid = payments.stream()
                .filter(p  -> "PAID".equalsIgnoreCase(p.getStatus().name()))
                .mapToDouble(LoanPayment::getAmountPaid)
                .sum();



        double totalLoanAmount = loans.stream()
                .mapToDouble(Loan::getAmount)
                .sum();

        double remainingBalance = 0.0;
        String progressMessage = "0";

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
                        .paymentStatus(payment.getStatus().name())
                        .build())
                .toList();

        UserAllLoansResponse response = UserAllLoansResponse.builder()
                .applications(applicationInfos)
                .loans(loanInfos)
                .payments(paymentInfos)
                .totalAmountPaid(BigDecimal.valueOf(totalPaid))
                .paymentProgress(progressMessage)
                .remainingBalance(remainingBalance)
                .userName(userInfo.get().getFirstName())
                .build();

        return new ApiResponse<>(
                true,
                "Successfully get data",
                response
        );
    }

    public String refNumberLoan(Long userId) {
        String prefix = "LOAN";
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long sequence = userId; // or use a counter from DB
        return prefix + "-" + datePart + "-" + sequence;
    }

    public long generateApplicationId(Long userId) {
        long timestamp = System.currentTimeMillis();
        return (timestamp * 100) + (userId % 100);
    }


}
