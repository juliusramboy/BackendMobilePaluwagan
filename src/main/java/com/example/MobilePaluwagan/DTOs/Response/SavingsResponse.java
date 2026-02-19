package com.example.MobilePaluwagan.DTOs.Response;

import com.example.MobilePaluwagan.DTOs.Request.PaymentFilterRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SavingsResponse {
        private boolean success;
        private String message;
        private PaymentFilterRequest filters; // or rename to just 'filters'
        private List<SavingsDepositHistory> savings;

}
