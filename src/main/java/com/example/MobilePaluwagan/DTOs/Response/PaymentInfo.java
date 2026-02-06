package com.example.MobilePaluwagan.DTOs.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class PaymentInfo {
    private Long loanId;
    private String referenceNumber;
    private BigDecimal amountPaid;
    private LocalDate paymentDate;
    private String paymentStatus;
    private String paymentMethod;
}
