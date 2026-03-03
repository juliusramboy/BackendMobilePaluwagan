package com.example.MobilePaluwagan.DTOs.Request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDepositSavingsRequest {

    @Pattern(regexp = "^[0-9]+(\\.[0-9]{1,2})?$")
    private double amountDeposit;

    @NotNull(message = "Date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate depositDate;
    private String reference;
    private String status;
}
