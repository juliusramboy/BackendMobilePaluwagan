package com.example.MobilePaluwagan.DTOs.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicantsAdmin {
    private Long id;
    private String fName;
    private String lName;
    private Double totalLoan;
    private Double weeklyPay;
}
