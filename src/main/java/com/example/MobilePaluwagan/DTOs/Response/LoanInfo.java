package com.example.MobilePaluwagan.DTOs.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
public class LoanInfo {
    private Long loanId;
    private BigDecimal loanAmount;
    private BigDecimal interestRate;
    private LocalDate startDate;
}
