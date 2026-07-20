package com.example.MobilePaluwagan.dto.Response;

import com.example.MobilePaluwagan.dto.Request.LedgerFilterRequest;
import com.example.MobilePaluwagan.dto.Request.PaymentFilterRequest;
import com.example.MobilePaluwagan.entity.Ledger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LedgerFilterResponse {
    private boolean success;
    private String message;
    private LedgerFilterRequest filters;
    private List<LedgerInfo> ledger;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private boolean last;
}
