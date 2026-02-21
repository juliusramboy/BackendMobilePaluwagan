package com.example.MobilePaluwagan.DTOs.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentFilterRequestAdmin {

    private String savingsId;
    private String reference;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String paymentMethod;

    public boolean hasFilters(){
        return reference != null || startDate != null || endDate != null || status != null || paymentMethod != null;
    }
}
