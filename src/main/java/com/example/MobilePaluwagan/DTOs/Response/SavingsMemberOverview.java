package com.example.MobilePaluwagan.DTOs.Response;

import com.example.MobilePaluwagan.Entity.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavingsMemberOverview {
    //private Long userId;
    private String firstName;
    private String lastName;

    private String savingsId;
    private BigDecimal savingsAccountBalance;
    private BigDecimal targetAmount;
    private boolean hasPendingPayment;
    private boolean hasPendingWithdrawal;
    private String profileImage;
}
