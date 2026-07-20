package com.example.MobilePaluwagan.dto.Request;

import com.example.MobilePaluwagan.entity.Description;
import com.example.MobilePaluwagan.entity.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LedgerFilterRequest {
    private Long userId;
    private String reference;
    private PaymentMethod method;
    private Description description;

    public boolean hasFilters(){
        return reference != null || method != null || description != null;
    }
}
