package com.example.MobilePaluwagan.dto.Request;

import lombok.Data;

@Data
public class OtpVerifyForgotRequest {
    private String userId;
    private String password;
    private String otp;
}
