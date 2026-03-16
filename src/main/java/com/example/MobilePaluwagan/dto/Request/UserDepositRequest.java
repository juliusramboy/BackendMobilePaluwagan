package com.example.MobilePaluwagan.dto.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDepositRequest {

    private LocalDate depositDate;
    private BigDecimal depositAmount;
}
