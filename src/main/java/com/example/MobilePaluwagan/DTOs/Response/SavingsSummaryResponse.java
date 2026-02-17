package com.example.MobilePaluwagan.DTOs.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SavingsSummaryResponse {
    private BigDecimal totalSavingsBalance;
    private BigDecimal annualMoney;
    private String savingsId;
    private boolean isTargetReached;
    private List<SavingsDepositHistory> depositHistoryList;
}
