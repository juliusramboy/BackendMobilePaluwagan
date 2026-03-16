package com.example.MobilePaluwagan.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SavingsMemberDTO {
    private String firstName;
    private String lastName;
    private String profileImage;
    private String savingsId;
    private BigDecimal targetAmount;
    private BigDecimal savingsAccountBalance;
    private boolean hasPendingPayment;
    private boolean hasPendingWithdrawal;

    private AdminTallySavings savingsTally;
}
