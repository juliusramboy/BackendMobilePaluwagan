package com.example.MobilePaluwagan.dto.Request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProfileUpdateRequest {

    private String firstName;
    private String middleName;
    private String lastName;
    private String suffix;
    private String gender;
    private String address;
    private String phoneNumber;
    private LocalDate birthDay;

    private String email;
    private String newPassword;
    private String oldPassword;
}
