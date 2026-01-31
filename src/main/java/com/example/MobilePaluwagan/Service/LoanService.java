package com.example.MobilePaluwagan.Service;

import com.example.MobilePaluwagan.DTOs.Request.ApplyLoanRequest;
import com.example.MobilePaluwagan.DTOs.Response.*;
import com.example.MobilePaluwagan.Entity.*;
import com.example.MobilePaluwagan.Repository.*;
import org.hibernate.query.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
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



//    public ApiResponse<UserApplyLoanResponse> loanApplication(Long userId, BigDecimal requestedAmount, LocalDate termLength, LocalDate startDate){
//        Optional<User> checkIfHaveLoan = userRepo.findById(userId);
//        Optional<Loan> checkIfHavePassLoan = userLoanRepo.findByUserId(userId);
//        Optional<LoanApplication> checkIfUserHaveAppllication = loanApplicationRepo.findByUserId(userId);
//
//
//        if(checkIfHaveLoan.isPresent() && checkIfHaveLoan.get().isHasLoan()){
//            return new ApiResponse<>(
//                    false,
//                    "We see that you have a pending Loan. Pay all your balance to make another loan.",
//                    null
//            );
//        }
//
//        boolean hasPendingApp = checkIfUserHaveAppllication.stream()
//                .anyMatch(app -> "PENDING".equalsIgnoreCase(app.getStatus().name()));
//
//        if (hasPendingApp) {
//            return new ApiResponse<>(
//                    false,
//                    "You already have a pending loan application. Please wait for approval.",
//                    null
//            );
//        }
//
//        if(checkIfHavePassLoan.isPresent()){
//            return new ApiResponse<>(
//                    false,
//                    "You already have a already loan. Please pay you all pending balance to loan again.",
//                    null
//            );
//        }
//
//        Long applicationNumber = generateApplicationId(userId);
//
//        LoanApplication loanApplication = new LoanApplication();
//        loanApplication.setApplicationID(applicationNumber);
//        loanApplication.setUserId(userId);
//        loanApplication.setRequestedAmount(requestedAmount);
//        loanApplication.setTermLength(termLength);
//        loanApplication.setApplicationDate(startDate);
//        loanApplication.setStatus(Status.PENDING);
//
//        LoanApplication saved = loanApplicationRepo.save(loanApplication);
//
//        UserApplyLoanResponse applyLoan = mapToUserLoanResponse(saved);
//
//        return new ApiResponse<>(
//                true,
//                "Successfully Applied loan pls wait for admin to verify it",
//                applyLoan
//        );
//    }

//    private UserApplyLoanResponse mapToUserLoanResponse(LoanApplication loan) {
//        return UserApplyLoanResponse.builder()
//                .applicationNumber(loan.getApplicationID())
//                .loanAmount(loan.getRequestedAmount())
//                .termLength(loan.getEndDate())
//                .status(loan.getStatus().name())
//                .applicationDate(loan.getStartDate())
//                .build();
//    }

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
                .mapToDouble(Loan::getTotalRepayable)
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
                        .endDate(app.getEndDate())
                        .startDate(app.getStartDate())
                        .status(app.getStatus().name())
                        .status(String.valueOf(app.getStatus()))
                        .build())
                .toList();

        List<LoanInfo> loanInfos = loans.stream()
                .map(loan -> LoanInfo.builder()
                        .loanId(loan.getId())
                        .totalLoan(BigDecimal.valueOf(loan.getAmount()))
                        .totalRepayable(BigDecimal.valueOf(loan.getTotalRepayable()))
                        .interestRate(BigDecimal.valueOf(loan.getInterestRate()))
                        .interest(BigDecimal.valueOf(loan.getInterest()))
                        .weeklyPay(BigDecimal.valueOf(loan.getWeeklyPay()))
                        .endDate(loan.getEndDate())
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


    public ApplyLoanResponse processLoanApplication(Long userId, BigDecimal loanAmount,
                                                    LocalDate startDate, LocalDate endDate) {
        // Step 1: Get duration
        LoanDurationResult duration = calculateLoanDuration(startDate, endDate);

        // Step 2: Calculate interest
        BigDecimal totalInterest = calculateLoanInterest(userId, loanAmount, duration.getTotalDays());

        // Step 3: Calculate payment schedule
        PaymentSchedule paymentSchedule = calculatePaymentSchedule(loanAmount, totalInterest, duration.getTotalWeeks());

        Long applicationId = generateApplicationId(userId);

//        // Format date range
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd-MMM dd");
//        String dateRange = startDate.format(DateTimeFormatter.ofPattern("MMM dd")) + "-" +
//                endDate.format(DateTimeFormatter.ofPattern("MMM dd"));

        // Calculate total repayable
        BigDecimal totalRepayable = loanAmount.add(totalInterest);

        // Get weeks and days
        int weeks = (int) Math.floor(duration.getTotalWeeks());
        int days = (int) duration.getTotalDays();

        // Return loan summary as ApplyLoanResponse (not ApiResponse)
        return new ApplyLoanResponse(
                applicationId,
                startDate,
                endDate,
                weeks,
                days,
                paymentSchedule.getRegularPayment(),
                loanAmount,
                totalInterest,
                totalRepayable
        );
    }

    // Step 1: Calculate loan duration
    private LoanDurationResult calculateLoanDuration(LocalDate startDate, LocalDate endDate) {
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
        double totalWeeks = (double) totalDays / 7;

        return new LoanDurationResult(totalDays, totalWeeks);
    }

    // Step 2: Calculate loan interest (MONTHLY RATE)
    private BigDecimal calculateLoanInterest(Long userId, BigDecimal loanAmount, long totalDays) {
        Optional<User> userOpt = userRepo.findById(userId);

        boolean hasSavings = userOpt.map(User::isHasSavings).orElse(false);

        // Monthly interest rate: 5% or 10% per month
        double monthlyRate = hasSavings ? 5.0 : 10.0;

        // Calculate daily rate based on 30-day month
        double dailyRate = (monthlyRate / 100) / 30;

        BigDecimal interestPerDay = loanAmount.multiply(BigDecimal.valueOf(dailyRate));
        BigDecimal totalInterest = interestPerDay.multiply(BigDecimal.valueOf(totalDays));

        // Round to 2 decimal places
        return totalInterest.setScale(2, RoundingMode.HALF_UP);
    }

    // Step 3: Calculate payment schedule
    private PaymentSchedule calculatePaymentSchedule(BigDecimal loanAmount, BigDecimal totalInterest, double totalWeeks) {
        int numberOfPayments = (int) Math.ceil(totalWeeks);
        BigDecimal totalAmountDue = loanAmount.add(totalInterest);

        // Calculate regular weekly payment - divide by NUMBER OF PAYMENTS
        BigDecimal regularPayment = totalAmountDue.divide(
                BigDecimal.valueOf(numberOfPayments),
                2,
                RoundingMode.HALF_UP
        );

        return new PaymentSchedule(numberOfPayments, regularPayment);
    }

    public Long applyLoan(Long userId, ApplyLoanRequest request) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        validateUserCanApplyForLoan(user);

        boolean hasSavings = user.isHasSavings();
        double monthlyRate = hasSavings ? 5.0 : 10.0;


        LoanApplication saveLoan = new LoanApplication();
        saveLoan.setApplicationID(request.getApplicationId());
        saveLoan.setUserId(userId);
        saveLoan.setStartDate(request.getStartDate());
        saveLoan.setEndDate(request.getEndDate());
        saveLoan.setRepayPeriodDays(request.getRepayPeriodDays());
        saveLoan.setRepayPeriodWeeks(request.getRepayPeriodWeeks());
        saveLoan.setWeeklyPay(request.getWeeklyPay());
        saveLoan.setRequestedAmount(request.getTotalLoan());
        saveLoan.setInterest(request.getInterest());
        saveLoan.setInterestRate(monthlyRate);
        saveLoan.setTotalRepayable(request.getTotalRepayable());
        saveLoan.setStatus(Status.PENDING);

        loanApplicationRepo.save(saveLoan);

        return request.getApplicationId();
    }

    private void validateUserCanApplyForLoan(User user) {

        if (user.isHasLoan()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You already have an active loan");
        }


        boolean hasActiveLoan = loanApplicationRepo.findAllByUserId(user.getId()).stream()
                .anyMatch(data -> data.getStatus() == Status.PENDING);

        if (hasActiveLoan){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "You already have an active or pending loan application");
        }
    }

    public LoanStatusResponse getUserLoanStatus(Long userId){

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        boolean hasLoan = user.isHasLoan();

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

        return new LoanStatusResponse(
                hasLoan,
                hasPendingApplication,
                hasApprovedApplication,
                latestApplication.map(LoanApplication::getApplicationID).orElse(null),
                latestApplication.map(app -> app.getStatus().name()).orElse(null)
        );
    }


    public Optional<LoanApplication> getDetails(Long userId){

        Optional<LoanApplication> latestApplication = loanApplicationRepo.findAllByUserId(userId).stream()
                .filter(app -> app.getStatus() == Status.PENDING || app.getStatus() == Status.APPROVED)
                .max(Comparator.comparing(LoanApplication::getApplicationID));

        return latestApplication;
    }


    // Inner classes
    private static class LoanDurationResult {
        private long totalDays;
        private double totalWeeks;

        public LoanDurationResult(long totalDays, double totalWeeks) {
            this.totalDays = totalDays;
            this.totalWeeks = totalWeeks;
        }

        public long getTotalDays() { return totalDays; }
        public double getTotalWeeks() { return totalWeeks; }
    }

    private static class PaymentSchedule {
        private int numberOfPayments;
        private BigDecimal regularPayment;

        public PaymentSchedule(int numberOfPayments, BigDecimal regularPayment) {
            this.numberOfPayments = numberOfPayments;
            this.regularPayment = regularPayment;
        }

        public int getNumberOfPayments() { return numberOfPayments; }
        public BigDecimal getRegularPayment() { return regularPayment; }
    }

}
