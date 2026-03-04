package com.example.MobilePaluwagan.DTOs.Request;


import com.example.MobilePaluwagan.Entity.PaymentMethod;
import com.example.MobilePaluwagan.Entity.Status;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentLoanRequest {

    private Long applicationId;
    private BigDecimal amount;
    private String reference;
    private PaymentMethod paymentMethod;
    @Nullable
    private String BankReference;
}
