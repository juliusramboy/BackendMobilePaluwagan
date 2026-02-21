package com.example.MobilePaluwagan.DTOs.Request;

import com.example.MobilePaluwagan.DTOs.Response.SavingsDepositHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SavingsResponseAdmin {
    private boolean success;
    private String message;
    private PaymentFilterRequestAdmin filters; // or rename to just 'filters'
    private List<SavingsDepositHistory> savings;
}
