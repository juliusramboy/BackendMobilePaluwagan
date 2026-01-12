package com.example.MobilePaluwagan.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Entity
@Table(name = "loan_closure")
@NoArgsConstructor
@AllArgsConstructor
public class LoanClosure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "loan_id")
    private Long loanId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "reference_number", length = 191)
    private String referenceNumber;

    @Column(name = "closure_date")
    private Date closureDate;

    @Column(name = "final_amount_paid")
    private Double finalAmountPaid;
}
