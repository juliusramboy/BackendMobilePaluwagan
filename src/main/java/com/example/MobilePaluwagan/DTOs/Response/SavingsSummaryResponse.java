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
    private BigDecimal targetAmount;
    private boolean hasWithdraw;
    private List<SavingsDepositHistory> depositHistoryList;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private boolean last;
}
