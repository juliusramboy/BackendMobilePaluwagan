package com.example.MobilePaluwagan.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "loan_application")
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "application_id")
    private Long applicationID;

    @Column(name = "user_id", updatable = false)
    private Long userId;

    @Column(name = "requested_amount")
    private BigDecimal requestedAmount;

    @Column(name = "interest")
    private double interest;

    @Column(name = "interest_rate")
    private double interestRate;

    @Column(name = "weekly_pay")
    private BigDecimal weeklyPay;

    @Column(name = "total_repayable")
    private BigDecimal totalRepayable;

    @Column(name = "repay_period_days")
    private int repayPeriodDays;

    @Column(name = "repay_period_weeks")
    private int repayPeriodWeeks;

    @Column(name = "end_date")
    private LocalDate  endDate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @JsonManagedReference
    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @ToString.Exclude
    private UserInfo userInfo;
}
