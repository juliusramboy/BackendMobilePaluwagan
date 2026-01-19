package com.example.MobilePaluwagan.DTOs.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SavingsSummaryResponse {
    private double totalSavingsBalance;
    private double annualMoney;
    private List<SavingsDepositHistory> depositHistoryList;
}
