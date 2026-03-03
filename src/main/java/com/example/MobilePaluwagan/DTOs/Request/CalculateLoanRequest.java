package com.example.MobilePaluwagan.DTOs.Request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalculateLoanRequest {
     @NotNull(message = "Start date required")
     @JsonFormat(pattern = "yyyy-MM-dd")
     LocalDate startDate;
     @NotNull(message = "End date is required")
     @JsonFormat(pattern = "yyyy-MM-dd")
     LocalDate endDate;
     @NotNull(message = "Loan amount is required")
     @DecimalMin(value = "1000.00", message = "Minimum loan amount is ₱1000.00")
     @DecimalMax(value = "20000.00", message = "Maximum loan amount is ₱20,000.00")
     BigDecimal loanAmount;
}
