package com.example.MobilePaluwagan.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class UserApplyLoanResponse {
    private Long applicationNumber;
    private BigDecimal loanAmount;
    private LocalDate termLength;
    private String status;
    private LocalDate applicationDate;

}
