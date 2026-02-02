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
public class paymentResponse {
    private boolean success;
    private String message;
    private PaymentFilterRequest filters;
    private List<PaymentInfo> payment;
}
