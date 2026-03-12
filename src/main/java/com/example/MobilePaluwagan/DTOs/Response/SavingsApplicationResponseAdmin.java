package com.example.MobilePaluwagan.DTOs.Response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SavingsApplicationResponseAdmin {
    private boolean success;
    private String message;
    private List<AdminPaymentSavingsSearchResponse> paymentSavings;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private boolean last;
}
