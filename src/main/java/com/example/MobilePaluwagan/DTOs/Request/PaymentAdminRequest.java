package com.example.MobilePaluwagan.DTOs.Request;


import com.example.MobilePaluwagan.Entity.PaymentMethod;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentAdminRequest {

    private String applicationId;
    private Double amount;
    private String reference;
    private PaymentMethod paymentMethod;
    @Nullable
    private String BankReference;
}
