package com.example.MobilePaluwagan.DTOs.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class UserAllLoansResponse {
    private List<LoanApplicationInfo> applications;
    private List<LoanInfo> loans;
    private List<PaymentInfo> payments;
}
