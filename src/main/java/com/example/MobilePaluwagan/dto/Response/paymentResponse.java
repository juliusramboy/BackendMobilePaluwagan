package com.example.MobilePaluwagan.dto.Response;

import com.example.MobilePaluwagan.dto.Request.PaymentFilterRequest;
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
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private boolean last;
}
