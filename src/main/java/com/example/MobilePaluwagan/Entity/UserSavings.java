package com.example.MobilePaluwagan.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "user_savings")
@NoArgsConstructor
@AllArgsConstructor
public class UserSavings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "savings_id")
    private String savingsId;

    @Column(name = "amount_deposit")
    private double amountDeposit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "deposit_date")
    private LocalDateTime depositDate;

    private String reference;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserInfo userInfo;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

//    @ManyToOne
//    @JsonIgnore
//    @JoinColumn(name = "user_id", insertable = false, updatable = false)
//    private SavingsWithdrawApplication savingsWithdrawApplication;

}
