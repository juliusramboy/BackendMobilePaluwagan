package com.example.MobilePaluwagan.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "user_bank")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserBank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "first_deposit_date")
    private LocalDate firstDepositDate;

    @Column(name = "account_balance")
    private Long accountBalance;

    @Column(name = "savings_id")
    private String savingsId;

    @Column(name = "target_amount")
    private BigDecimal targetAmount;

    @Column(name = "has_savings_deposit")
    private boolean hasSavingsDeposit;
}
