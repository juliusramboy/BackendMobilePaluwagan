package com.example.MobilePaluwagan.dto.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerificationRequest {
    private Long userId;
    private String otpHash;
    private LocalDateTime expiresAt;
    private LocalDateTime createAt;
}
