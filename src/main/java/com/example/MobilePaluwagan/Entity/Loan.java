package com.example.MobilePaluwagan.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Data
@Entity
@Table(name = "loan")
@NoArgsConstructor
@AllArgsConstructor
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "application_id")
    private Long applicationID;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "total_loan")
    private Double amount;

    @Column(name = "total_repayable")
    private Double totalRepayable;

    @Column(name = "weekly_pay")
    private Double weeklyPay;

    @Column(name = "interest")
    private Double interest;

    @Column(name = "interest_rate")
    private Double interestRate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;
}
