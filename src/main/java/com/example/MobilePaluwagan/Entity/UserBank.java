package com.example.MobilePaluwagan.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Column(name = "target_amount")
    private Long targetAmount;

    @Column(name = "account_balance")
    private Long accountBalance;
}
