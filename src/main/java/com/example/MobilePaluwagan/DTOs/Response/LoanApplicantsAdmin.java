package com.example.MobilePaluwagan.DTOs.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicantsAdmin {
    private Long userId;
    private Long applicationId;
    private BigDecimal totalRepayable;
    private BigDecimal weeklyPay;
    private String firstName;
    private String lastName;
}
