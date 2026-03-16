package com.example.MobilePaluwagan.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class LoanApplicationInfo {
    private Long applicationNumber;
    private BigDecimal loanAmount;
    private LocalDate startDate;
    private String status;
    private LocalDate endDate;
}
