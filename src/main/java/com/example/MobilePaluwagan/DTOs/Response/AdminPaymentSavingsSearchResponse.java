package com.example.MobilePaluwagan.DTOs.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminPaymentSavingsSearchResponse {
    private String savingsId;
    private BigDecimal accountBalance;
    private String firstName;
    private String lastName;
    private String profileImage;

}
