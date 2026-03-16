package com.example.MobilePaluwagan.dto.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    @NotBlank(message = "First Name is required")
    @Pattern(
            regexp = "^$|^[a-zA-Z]+$",
            message = "First Name must only contain letters")
    private String firstName;
    @Pattern(
            regexp = "^$|^[a-zA-Z]+$",
            message = "Middle  Name must only contain letters")
    private String middleName;
    @NotBlank(message = "Last Name is required")
    @Pattern(
            regexp = "^$|^[a-zA-Z]+$",
            message = "Last Name must only contain letters")
    private String lastName;
    private String suffix;
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid, e.g. example@gmail.com")
    private String email;
    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^09[0-9]{9}$",
            message = "Phone number must be a valid PH mobile number, e.g. 09123456789")
    private String phoneNumber;
    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
            message = "Password must be at least 8 characters with uppercase and lowercase")
    private String password;


    private Integer roleId;
    private Long targetAmount;
    private Long accountBalance;
    private Boolean isActive;

    private String otpHash;
    private LocalDateTime expiresAt;
    private LocalDateTime createAt;

}
