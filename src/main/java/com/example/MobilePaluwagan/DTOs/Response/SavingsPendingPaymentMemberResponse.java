package com.example.MobilePaluwagan.DTOs.Response;

import com.example.MobilePaluwagan.Entity.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SavingsPendingPaymentMemberResponse {
    private double amountDeposit;
    private LocalDateTime depositDate;
    private String reference;
    private Status status;
}
