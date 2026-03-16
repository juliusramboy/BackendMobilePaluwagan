package com.example.MobilePaluwagan.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SavingsDetailResponse {
    private UserSavingsInfo user;
    private List<SavingsPendingPaymentMemberResponse> payments;
    private WithdrawSavingsInfo withdraw;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private boolean last;
}
