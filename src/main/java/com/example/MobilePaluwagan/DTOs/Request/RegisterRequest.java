package com.example.MobilePaluwagan.DTOs.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String username;
    private String password;
    private Integer roleId;

}
