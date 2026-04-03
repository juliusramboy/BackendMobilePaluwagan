package com.example.MobilePaluwagan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "paymongo_payment")
@NoArgsConstructor
@AllArgsConstructor
public class PaymongoPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    // PayMongo specific fields
    @Column(name = "paymongo_link_id")
    private String paymongoLinkId;

    @Column(name = "payment_type")  // ← "LOAN" or "SAVINGS"
    private String paymentType;

    @Column(name = "reference_id")  // ← loanId or savingsId
    private String referenceId;

    @Column(name = "reference_number", length = 191)
    private String referenceNumber;

    @Column(name = "checkout_url", length = 500)
    private String checkoutUrl;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

}
