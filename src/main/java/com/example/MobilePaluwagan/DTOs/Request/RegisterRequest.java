package com.example.MobilePaluwagan.DTOs.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private Long userId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String suffix;
    private String email;
    private String phoneNumber;
    private String username;
    private String password;
    private Integer roleId;
    private Long targetAmount;
    private Long accountBalance;
    private Boolean isActive;

    private String otpHash;
    private LocalDateTime expiresAt;
    private LocalDateTime createAt;

}
