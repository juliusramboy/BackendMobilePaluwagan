package com.example.MobilePaluwagan.dto.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentFilterSavingsRequest {

    private Long userId;
    private String reference;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public boolean hasFilters(){
        return  reference != null || startDate != null || endDate != null;
    }
}
