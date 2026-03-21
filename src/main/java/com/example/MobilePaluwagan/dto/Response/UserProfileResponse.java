package com.example.MobilePaluwagan.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {
    private String firstName;
    private String lastName;
    private String middlleName;
    private String suffix;
    private String phoneNumber;
    private LocalDate verifiedDate;
    private String address;
    private LocalDate birthday;
    private String gender;
    private String profileImage;
    private boolean isOnline;

    private String email;
    private BigDecimal savingsBalance;
    private boolean isMature;
    private BigDecimal loanBalance;
    private LocalDate loanDueDate;

}
