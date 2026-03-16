package com.example.MobilePaluwagan.dto.Response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class OtpLoginResponse {
    private String message;
    private boolean success;

    public OtpLoginResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
    }
}
