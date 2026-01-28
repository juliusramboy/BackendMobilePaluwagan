package com.example.MobilePaluwagan.DTOs.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplyLoanResponse {
    private String dateRange;
    private int repayPeriodWeeks;
    private int repayPeriodDays;
    private BigDecimal weeklyPay;
    private BigDecimal totalLoan;
    private BigDecimal interest;
    private BigDecimal totalRepayable;
}
