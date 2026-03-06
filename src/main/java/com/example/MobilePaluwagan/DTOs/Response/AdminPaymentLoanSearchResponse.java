package com.example.MobilePaluwagan.DTOs.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class AdminPaymentLoanSearchResponse {
    private Long applicationId;
    private BigDecimal weeklyPay;
    private String firstName;
    private String lastName;
    private BigDecimal remainingBalance;

    public AdminPaymentLoanSearchResponse(Long applicationId, BigDecimal weeklyPay, String firstName, String lastName, BigDecimal totalRepayable, BigDecimal loanRepaymentTally) {
        this.applicationId = applicationId;
        this.weeklyPay = weeklyPay;
        this.firstName = firstName;
        this.lastName = lastName;
        this.remainingBalance = totalRepayable.subtract(loanRepaymentTally);
    }
}
