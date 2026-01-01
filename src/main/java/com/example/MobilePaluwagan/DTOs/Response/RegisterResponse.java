package com.example.MobilePaluwagan.DTOs.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    private String email;
    private Long userId;
    private String otp;
    private String message;


    public RegisterResponse(String email, long userId, String verificationToken, String message) {
        this.email = email;
        this.userId = userId;
        this.message = message;
    }
}
