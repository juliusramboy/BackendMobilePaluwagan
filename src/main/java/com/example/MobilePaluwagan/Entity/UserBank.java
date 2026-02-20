package com.example.MobilePaluwagan.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
    private LocalDateTime firstDepositDate;

    @Column(name = "account_balance")
    private BigDecimal accountBalance;

    @Column(name = "savings_id")
    private String savingsId;

    @Column(name = "target_amount")
    private BigDecimal targetAmount;

    @Column(name = "has_savings_deposit")
    private boolean hasSavingsDeposit;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}
