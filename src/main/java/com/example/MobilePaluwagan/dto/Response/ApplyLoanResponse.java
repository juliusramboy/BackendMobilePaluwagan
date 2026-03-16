package com.example.MobilePaluwagan.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplyLoanResponse {
    private Long applicationId;
    private LocalDate startDate;
    private LocalDate endDate;
    private int repayPeriodWeeks;
    private int repayPeriodDays;
    private BigDecimal weeklyPay;
    private BigDecimal totalLoan;
    private BigDecimal interest;
    private BigDecimal totalRepayable;
}
