package com.example.MobilePaluwagan.DTOs.Request;

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
    private LocalDate bday;

    private String email;
    private String password;
}
