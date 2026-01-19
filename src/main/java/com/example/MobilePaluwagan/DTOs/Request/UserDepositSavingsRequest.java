package com.example.MobilePaluwagan.DTOs.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class UserDepositSavingsRequest {
    private double amountDeposit;
    private LocalDate depositDate;
    private String reference;
    private String status;
}
