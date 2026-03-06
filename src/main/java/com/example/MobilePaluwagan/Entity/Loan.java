package com.example.MobilePaluwagan.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "loan")
@NoArgsConstructor
@AllArgsConstructor
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "application_id")
    private Long applicationID;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "total_loan")
    private BigDecimal amount;

    @Column(name = "total_repayable")
    private BigDecimal totalRepayable;

    @Column(name = "weekly_pay")
    private BigDecimal weeklyPay;

    @Column(name = "interest")
    private Double interest;

    @Column(name = "interest_rate")
    private Double interestRate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "loan_repayment_tally", nullable = false)
    private BigDecimal loanRepaymentTally =  BigDecimal.ZERO;
}
