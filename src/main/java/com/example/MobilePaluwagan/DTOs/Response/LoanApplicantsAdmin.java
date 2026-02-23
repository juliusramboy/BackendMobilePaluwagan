package com.example.MobilePaluwagan.DTOs.Response;

import com.example.MobilePaluwagan.Entity.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicantsAdmin {
    private Long userId;
    private Long applicationId;
    private BigDecimal totalRepayable;
    private BigDecimal weeklyPay;

    private BigDecimal requestedAmount;
    private double interestRate;
    private double interest;
    private int repayPeriodDays;
    private int repayPeriodWeeks;
    private LocalDate startDate;
    private LocalDate endDate;
    private Status status;

    private String firstName;
    private String lastName;
    private String profileImage;
}
