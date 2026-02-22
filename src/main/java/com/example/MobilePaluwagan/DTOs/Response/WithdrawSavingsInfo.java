package com.example.MobilePaluwagan.DTOs.Response;

import com.example.MobilePaluwagan.Entity.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawSavingsInfo {

    private LocalDateTime withdrawDate;
    private String reference;
    private BigDecimal totalBalance;
    private Status status;
}
