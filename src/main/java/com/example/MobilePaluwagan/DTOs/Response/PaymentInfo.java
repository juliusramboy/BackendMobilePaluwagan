package com.example.MobilePaluwagan.DTOs.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
public class PaymentInfo {
    private Long paymentId;
    private String referenceNumber;
    private BigDecimal amountPaid;
    private LocalDate paymentDate;
    private String paymentMethod;
}
