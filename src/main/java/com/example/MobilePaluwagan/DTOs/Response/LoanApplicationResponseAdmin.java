package com.example.MobilePaluwagan.DTOs.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationResponseAdmin {
    private boolean success;
    private String message;
    private List<AdminPaymentLoanSearchResponse> paymentLoans;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private boolean last;
}
