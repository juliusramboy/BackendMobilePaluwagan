package com.example.MobilePaluwagan.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicantsFullInfoAdmin {
    private Long applicationID;
    private BigDecimal requestedAmount;
    private double interestRate;          // Keep as double
    private double interest;              // Keep as double
    private BigDecimal weeklyPay;
    private BigDecimal totalRepayable;
    private int repayPeriodDays;          // Keep as int
    private int repayPeriodWeeks;         // Keep as int
    private LocalDate startDate;
    private LocalDate endDate;
    private String firstName;
    private String lastName;
    private String profileImage;
}