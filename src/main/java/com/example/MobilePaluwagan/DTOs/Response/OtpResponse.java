package com.example.MobilePaluwagan.DTOs.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor

public class OtpResponse {
    private String message;
    private Long userId;

    public OtpResponse(String message, Long userId) {
        this.message = message; this.userId = userId;
    }
}
