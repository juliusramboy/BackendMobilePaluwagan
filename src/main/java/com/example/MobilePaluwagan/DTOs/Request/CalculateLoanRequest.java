package com.example.MobilePaluwagan.DTOs.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalculateLoanRequest {
     LocalDate startDate;
     LocalDate endDate;
     BigDecimal loanAmount;
}
