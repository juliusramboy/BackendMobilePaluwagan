package com.example.MobilePaluwagan.dto.Response;

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
