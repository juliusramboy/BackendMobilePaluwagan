package com.example.MobilePaluwagan.dto.Response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SavingsDepositHistory {
    private double amountRemit;
    private LocalDateTime remitDate;
    private String reference;
    private String status;

}
