package com.example.MobilePaluwagan.dto.Response;

import com.example.MobilePaluwagan.entity.Description;
import com.example.MobilePaluwagan.entity.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class LedgerInfo {
    private BigDecimal amount;
    private LocalDateTime depositDate;
    private String reference;
    private Description description;
    private PaymentMethod paymentMethod;
}
