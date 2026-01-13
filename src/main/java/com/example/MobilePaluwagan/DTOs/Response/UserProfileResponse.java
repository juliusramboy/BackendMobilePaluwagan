package com.example.MobilePaluwagan.DTOs.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class UserProfileResponse {
    private String firstName;
    private String lastName;
    private String middlleName;
    private String suffix;
    private String phoneNumber;
    private LocalDate verifiedDate;

    private String email;


}
