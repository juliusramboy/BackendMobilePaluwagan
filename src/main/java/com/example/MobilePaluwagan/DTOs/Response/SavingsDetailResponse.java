package com.example.MobilePaluwagan.DTOs.Response;

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
}
