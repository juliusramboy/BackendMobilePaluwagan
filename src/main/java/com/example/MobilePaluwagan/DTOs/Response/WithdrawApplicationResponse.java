package com.example.MobilePaluwagan.DTOs.Response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class WithdrawApplicationResponse {
    private String savingsId;
    private BigDecimal totalWithdrawal;
    private String reference;
    private String status;
}
