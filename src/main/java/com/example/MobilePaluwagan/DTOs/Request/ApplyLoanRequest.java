package com.example.MobilePaluwagan.DTOs.Request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
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
    @NotNull(message = "Start date required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @NotNull(message = "End date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    private int repayPeriodWeeks;
    private int repayPeriodDays;
    private BigDecimal weeklyPay;
    private BigDecimal totalLoan;
    private Double interest;
    private Double interestRate;
    private BigDecimal totalRepayable;
}
