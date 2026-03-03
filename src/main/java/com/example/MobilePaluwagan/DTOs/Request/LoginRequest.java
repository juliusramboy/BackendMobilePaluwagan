package com.example.MobilePaluwagan.DTOs.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @Email(message = "invalid email format")
    private String email;

    private String password;

    @Pattern(regexp = "^[0-9]{6}$")
    private String otp;
}
