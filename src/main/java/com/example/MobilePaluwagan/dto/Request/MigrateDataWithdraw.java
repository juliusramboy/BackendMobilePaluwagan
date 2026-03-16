package com.example.MobilePaluwagan.dto.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MigrateDataWithdraw {
    private Long userId;
    private String savingsId;
    private BigDecimal amount;
    private LocalDateTime depositDate;
    private String reference;
}
