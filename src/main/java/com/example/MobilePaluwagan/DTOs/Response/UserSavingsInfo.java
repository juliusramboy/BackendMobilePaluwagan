package com.example.MobilePaluwagan.DTOs.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSavingsInfo {
    private String firstName;
    private String lastName;
    private BigDecimal targetAmount;
    private String savingsId;
    private BigDecimal accountBalance;
    private LocalDateTime maturityDate;
    private String profileImage;
}
