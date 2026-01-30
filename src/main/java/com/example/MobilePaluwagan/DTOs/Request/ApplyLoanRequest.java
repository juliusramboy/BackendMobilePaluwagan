package com.example.MobilePaluwagan.DTOs.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplyLoanRequest {
    private Long applicationId;
    private LocalDate startDate;
    private LocalDate endDate;
    private int repayPeriodWeeks;
    private int repayPeriodDays;
    private BigDecimal weeklyPay;
    private BigDecimal totalLoan;
    private Double interest;
    private Double interestRate;
    private BigDecimal totalRepayable;
}
