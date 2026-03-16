package com.example.MobilePaluwagan.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class PaymentInfo {
    private Long loanId;
    private String referenceNumber;
    private BigDecimal amountPaid;
    private LocalDateTime paymentDate;
    private String paymentStatus;
    private String paymentMethod;
}
