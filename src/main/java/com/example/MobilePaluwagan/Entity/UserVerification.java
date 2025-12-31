package com.example.MobilePaluwagan.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_verification")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;
    @Column(name = "otp_hash")
    private String otpHash;
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

}
