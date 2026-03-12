package com.example.MobilePaluwagan.Service;

import com.example.MobilePaluwagan.Controller.SseController;
import com.example.MobilePaluwagan.DTOs.Request.AdminLoanStatus;
import com.example.MobilePaluwagan.DTOs.Request.ApplyLoanRequest;
import com.example.MobilePaluwagan.DTOs.Request.PaymentFilterRequest;
import com.example.MobilePaluwagan.DTOs.Request.WeeklyAmortizationSchedule;
import com.example.MobilePaluwagan.DTOs.Response.*;
import com.example.MobilePaluwagan.Entity.*;
import com.example.MobilePaluwagan.Repository.*;
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
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

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

    @Autowired
    private SseController sseController;

    @Autowired
    private DueDateScheduleRepository dueDateScheduleRepo;

    @Autowired
    private NotificationService notificationService;





    public ApiResponse<UserAllLoansResponse> getAllTheInfo(Long userId){
        Optional<UserInfo> userInfo = userInfoRepo.findByUserId(userId);
        List<LoanApplication> applications = loanApplicationRepo.findAllByUserId(userId);
        List<Loan> loans = userLoanRepo.findAllByUserId(userId);
        List<LoanPayment> payments = loanPaymentRepo.findPaymentsByUserIdNative(userId);


        if (applications.isEmpty()) {
            UserAllLoansResponse response = UserAllLoansResponse.builder()
                    .applications(List.of())
                    .loans(List.of())
                    .payments(List.of())
                    .totalAmountPaid(BigDecimal.ZERO)
                    .paymentProgress("")
                    .remainingBalance(BigDecimal.valueOf(0.0))
                    .userName(userInfo.get().getFirstName())
                    .eligible(true)
                    .build();

            return new ApiResponse<>(true, "User is eligible to make loan", response);
        }


        BigDecimal totalPaid = Optional.ofNullable(loanPaymentRepo.sumAllPaidByUserId(userId))
                .orElse(BigDecimal.ZERO);



        BigDecimal totalLoanAmount = Optional.ofNullable(userLoanRepo.sumTotalRepayableByUserId(userId))
                .orElse(BigDecimal.ZERO);


        BigDecimal remainingBalance = BigDecimal.ZERO;
        String progressMessage = "0";

        if (totalLoanAmount.compareTo(BigDecimal.ZERO) > 0) {
            remainingBalance = totalLoanAmount.subtract(totalPaid);

            if (remainingBalance.compareTo(BigDecimal.ZERO) < 0){
                remainingBalance = BigDecimal.ZERO;
            };

            BigDecimal percentPaid = totalPaid.divide(totalLoanAmount, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

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
                        .totalLoan(loan.getAmount())
                        .totalRepayable(loan.getTotalRepayable())
                        .interestRate(BigDecimal.valueOf(loan.getInterestRate()))
                        .interest(BigDecimal.valueOf(loan.getInterest()))
                        .weeklyPay(loan.getWeeklyPay())
                        .endDate(loan.getEndDate())
                        .startDate(loan.getStartDate())
                        .build())
                .toList();


        UserAllLoansResponse response = UserAllLoansResponse.builder()
                .applications(applicationInfos)
                .loans(loanInfos)
                .totalAmountPaid(totalPaid)
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
        return (timestamp * 1_000_000) + userId;
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
        int weeks = (int) Math.ceil(duration.getTotalWeeks());
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

        boolean hasSavings = userOpt.map(User::isHasSavingsAccount).orElse(false);

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

        UserInfo userInfo = userInfoRepo.findByUserId(userId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "UserId not found in Users"));


        validateUserCanApplyForLoan(user);

        boolean hasSavings = user.isHasSavingsAccount();
        double monthlyRate = hasSavings ? 5.0 : 10.0;


        LoanApplication saveLoan = new LoanApplication();
        saveLoan.setApplicationID(request.getApplicationId());
        saveLoan.setUserId(user.getId());
//        System.out.println("userId "+ userId);
//        System.out.println("user.getID "+ user.getId());
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
        LoanApplication verified = loanApplicationRepo.findById(saveLoan.getId()).orElse(null);

        notificationService.notifyLoanSubmitted(String.valueOf(request.getApplicationId()), userInfo.getFirstName());

        return request.getApplicationId();
    }

    private void validateUserCanApplyForLoan(User user) {

        UserInfo info = userInfoRepo.findByUserId(user.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (info.getAddress() == null || info.getBirthDay() == null|| info.getFirstName() == null || info.getGender() == null || info.getLastName() == null || info.getMiddleName() == null || info.getPhoneNumber() == null || info.getSuffix() == null || info.getProfileImage() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please provide all required personal details before proceeding");
        }

        if (user.isHasLoan()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You already have an active loan");
        }



        boolean hasActiveLoan = loanApplicationRepo.existsByUserIdAndStatus(user.getId(), Status.PENDING);

        if (hasActiveLoan){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "You already have an active or pending loan application");
        }
    }



    public Optional<LoanApplication> getDetails(Long userId){

        Optional<LoanApplication> latestApplication = loanApplicationRepo
                .findTopByUserIdAndStatusInOrderByApplicationIDDesc(
                        userId,
                        List.of(Status.PENDING, Status.APPROVED)
                );

        return latestApplication;
    }

    public LoanApplicationResponse getPendingApplicants(Status status, int page, int size){
        Pageable pageable = PageRequest.of(page, size);

        Page<LoanApplicantsAdmin> applicantsAdminPage = loanApplicationRepo.findLoanByStatus(status, pageable);

        return LoanApplicationResponse.builder()
                .success(true)
                .message("Successfully retrieved approved applicants")
                .applicants(applicantsAdminPage.getContent())
                .currentPage(applicantsAdminPage.getNumber())
                .totalPages(applicantsAdminPage.getTotalPages())
                .totalElements(applicantsAdminPage.getTotalElements())
                .last(applicantsAdminPage.isLast())
                .build();
    }

//    public ApiResponse<List<LoanApplicantsAdmin>> getApproveApplicants(){
//        return new ApiResponse<>(
//                true,
//                "Successful",
//                loanApplicationRepo.findLoanByStatus(Status.APPROVED)
//        );
//    }
//
//    public ApiResponse<List<LoanApplicantsAdmin>> getRejectedApplicants(){
//        return new ApiResponse<>(
//                true,
//                "Successful",
//                loanApplicationRepo.findLoanByStatus(Status.REJECTED)
//        );
//    }

    public ApplicantsFullInfoAdmin applicantsFullInfo(Long applicationID){
        return loanApplicationRepo.findLoanApplicantsFullInfo(applicationID);
    }

//    public List<LoanPayment> filter(PaymentFilterRequest filter) {
//        return loanPaymentRepo.findByFilters(
//                filter.getReference(),
//                filter.getStartDate(),
//                filter.getEndDate(),
//                filter.getStatus(),
//                filter.getPaymentMethod()
//        );
//    }

    public Page<LoanPayment> filterUserPayments(PaymentFilterRequest filter, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return loanPaymentRepo.findPaymentsByUserIdWithFilters(
                filter.getUserId(),
                filter.getReference(),
                filter.getStartDate(),
                filter.getEndDate(),
                filter.getStatus(),
                filter.getPaymentMethod(),
                pageable
        );
    }

    public PaymentInfo convertToDTO(LoanPayment payment) {
        return PaymentInfo.builder()
                .loanId(payment.getLoanId())
                .referenceNumber(payment.getReferenceNumber())
                .amountPaid(payment.getAmountPaid())
                .paymentDate(payment.getPaymentDate())
                .paymentStatus(payment.getStatus().name())
                .paymentMethod(payment.getPaymentMethod().name())
                .build();
    }

    @Transactional
    public ApiResponse<?> loanAdminChangeStats(AdminLoanStatus request){

        Optional<LoanApplication> applicantId = loanApplicationRepo.findByApplicationID(request.getApplicationID());
       LoanApplication id = applicantId.get();


       if (id.getStatus() == Status.APPROVED && request.getStatus().equals(Status.APPROVED)){
           return new ApiResponse<>(
                   false,
                   "Status is already " + request.getStatus(),
                   null

           );
       }

       if (request.getStatus().equals(Status.REJECTED)){
               LoanApplication application = loanApplicationRepo.findByApplicationID(request.getApplicationID()).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application Id not found in Loan Application"));
               loanApplicationRepo.delete(application);
               notificationService.notifyLoanRejected(id.getId(), String.valueOf(request.getApplicationID()));
                sseController.notifyUpdate();
               return new ApiResponse<>(
                       true,
                       "Successful Rejected the status of applicant and deleted" + id.getStatus(),
                       null
               );
           }

        id.setStatus(request.getStatus());
        loanApplicationRepo.save(id);

        Loan loan = new Loan();
        loan.setApplicationID(id.getApplicationID());
        loan.setUserId(id.getUserId());
        loan.setAmount(id.getRequestedAmount());
        loan.setTotalRepayable(id.getTotalRepayable());
        loan.setWeeklyPay(id.getWeeklyPay());
        loan.setInterest(id.getInterest());
        loan.setInterestRate(id.getInterestRate());
        loan.setStartDate(id.getStartDate());
        loan.setEndDate(id.getEndDate());
        userLoanRepo.save(loan);

        Optional<User> user = userRepo.findById(id.getUserId());
        User changeTrue = user.get();
        changeTrue.setHasLoan(true);
        userRepo.save(changeTrue);

        generateSchedule(request.getApplicationID());
        notificationService.notifyLoanApproved(id.getUserId(), String.valueOf(request.getApplicationID()));
        sseController.notifyUpdate();

        return new ApiResponse<>(
                true,
                "Successful change the status of applicant " + id.getStatus(),
                null
        );
    }

    public Map<String, Long> getStatusCounts(){
        Map<String, Long> counts = new HashMap<>();

        counts.put("PENDING",loanApplicationRepo.countByStatus(Status.PENDING));
        counts.put("REJECTED", loanApplicationRepo.countByStatus(Status.REJECTED));
        counts.put("APPROVED", loanApplicationRepo.countByStatus(Status.APPROVED));

        return counts;
    }

    @Transactional
    public void generateSchedule(Long applicationId){
        LoanApplication application = loanApplicationRepo.findByApplicationID(applicationId).orElseThrow(()-> new RuntimeException("Application Id not found in Loan Application"));

        List<DueDateSchedule> schedule = new ArrayList<>();

        BigDecimal balance = application.getTotalRepayable();

        for (int week = 1; week <= application.getRepayPeriodWeeks(); week++){
            LocalDate dueDate = application.getStartDate().plusWeeks(week);

            BigDecimal payment;
            if(week == application.getRepayPeriodWeeks()) {
                payment = balance;
            } else {
                payment = application.getWeeklyPay();
            }

            balance = balance.subtract(application.getWeeklyPay())
                    .setScale(2, RoundingMode.HALF_UP);

            DueDateSchedule entry = new DueDateSchedule();
            entry.setApplicationId(application.getApplicationID());
            entry.setWeek(week);
            entry.setDueDate(dueDate);
            entry.setPayment(payment);
            entry.setRemainingBalance(balance);
            entry.setStatus(Status.PENDING);

            schedule.add(entry);
        }
        dueDateScheduleRepo.saveAll(schedule);
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
