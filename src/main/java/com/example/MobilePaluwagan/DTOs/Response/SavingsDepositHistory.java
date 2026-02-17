package com.example.MobilePaluwagan.DTOs.Response;

import com.example.MobilePaluwagan.Entity.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class SavingsDepositHistory {
    private double amountRemit;
    private LocalDateTime remitDate;
    private String reference;
    private String status;

}
