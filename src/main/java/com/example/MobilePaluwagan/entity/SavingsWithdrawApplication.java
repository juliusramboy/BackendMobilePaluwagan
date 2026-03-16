package com.example.MobilePaluwagan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "savings_withdraw_applciation")
@NoArgsConstructor
@AllArgsConstructor
public class SavingsWithdrawApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "savings_id")
    private String savingsId;

    @Column(name = "account_balance")
    private BigDecimal accountBalance;

    @Column(name = "withdraw_date")
    private LocalDateTime withdrawDate;

    @Column(name = "target_amount")
    private BigDecimal targetAmount;

    private BigDecimal annual;

    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @ToString.Exclude
    private User user;

}
