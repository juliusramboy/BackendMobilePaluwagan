package com.example.MobilePaluwagan.DTOs.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor

public class LoginResponse {
    private String status;
    private String token;
    private Date expiry;

    public LoginResponse(String status, String token, Date expiry) {
        this.status = status;
        this.token = token;
        this.expiry = expiry;
    }

    public LoginResponse(String email, Long id, Object o, String s) {
        this.status = status;
        this.token = token;
        this.expiry = expiry;
    }
}
