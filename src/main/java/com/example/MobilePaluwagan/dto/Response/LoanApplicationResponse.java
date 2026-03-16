package com.example.MobilePaluwagan.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationResponse {
    private boolean success;
    private String message;
    private List<LoanApplicantsAdmin> applicants;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private boolean last;
}
