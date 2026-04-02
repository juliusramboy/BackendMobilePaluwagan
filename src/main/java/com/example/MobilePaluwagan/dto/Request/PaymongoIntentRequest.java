package com.example.MobilePaluwagan.dto.Request;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class PaymongoIntentRequest {
    private String description;
    private BigDecimal amount;
    private String paymentType;  // "LOAN" or "SAVINGS"
    private String referenceId;  // loanId or savingsId
    private String methodType;
}
