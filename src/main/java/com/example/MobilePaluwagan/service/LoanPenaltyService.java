package com.example.MobilePaluwagan.service;

import com.example.MobilePaluwagan.entity.Loan;
import com.example.MobilePaluwagan.entity.LoanPayment;
import com.example.MobilePaluwagan.repository.DueDateScheduleRepository;
import com.example.MobilePaluwagan.repository.LoanPaymentRepo;
import com.example.MobilePaluwagan.repository.UserLoanRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoanPenaltyService {

    private final UserLoanRepo userLoanRepo;
    private final LoanPaymentRepo loanPaymentRepo;


    @Scheduled(cron = "0 0 15 * * *")
    public void checkAndApplyPenalties() {

        LocalDate today = LocalDate.now();

        // Get all loans that are overdue and not fully paid
        List<Loan> overdueLoans = userLoanRepo.findOverdueLoans(today);

        for (Loan loan : overdueLoans) {
            applyPenalty(loan, today);
        }
    }

    private void applyPenalty(Loan loan, LocalDate today) {

        // Get last payment date
        Optional<LoanPayment> lastPaymentOpt = loanPaymentRepo
                .findTopByLoanIdOrderByPaymentDateDesc(loan.getId());

        LocalDate baseDate = lastPaymentOpt
                .map(p -> p.getPaymentDate().toLocalDate())
                .orElse(loan.getEndDate()); // if no payment, base from end date

        // Calculate days since last payment
        long overdueDays = ChronoUnit.DAYS.between(baseDate, today);

        if (overdueDays <= 0) return;

        // Remaining balance
        BigDecimal remainingBalance = loan.getTotalRepayable()
                .subtract(loan.getLoanRepaymentTally())
                .setScale(2, RoundingMode.HALF_UP);

        if (remainingBalance.compareTo(BigDecimal.ZERO) <= 0) return;

        // Daily rate = interestRate / 365
        BigDecimal dailyRate = BigDecimal.valueOf(loan.getInterestRate() / 100 / 365)
                .setScale(10, RoundingMode.HALF_UP);

        // Penalty for 1 day only (runs daily!)
        BigDecimal penalty = remainingBalance
                .multiply(dailyRate)
                .setScale(2, RoundingMode.HALF_UP);



        // Add penalty to totalRepayable
        loan.setTotalRepayable(loan.getTotalRepayable().add(penalty));
        userLoanRepo.save(loan);

    }
}
