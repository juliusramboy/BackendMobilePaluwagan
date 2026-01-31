package com.example.MobilePaluwagan.DTOs.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicantsAdmin {
    private Long id;
    private String firstName;
    private String lastName;
    private BigDecimal totalRepayable;
    private BigDecimal weeklyPay;
}
