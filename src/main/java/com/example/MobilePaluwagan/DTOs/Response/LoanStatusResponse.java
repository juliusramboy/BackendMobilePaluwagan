package com.example.MobilePaluwagan.DTOs.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanStatusResponse {
    private boolean hasActiveLoan;
    private boolean hasPendingApplication;
    private boolean hasApprovedApplication;
    private Long latestApplicationId;
    private String latestApplicationStatus;
}
