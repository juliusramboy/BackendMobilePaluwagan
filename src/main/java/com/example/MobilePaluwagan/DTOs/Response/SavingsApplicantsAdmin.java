package com.example.MobilePaluwagan.DTOs.Response;

import com.example.MobilePaluwagan.Entity.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavingsApplicantsAdmin {
    private String firstName;
    private String lastName;

    private double amountDeposit;
    private LocalDateTime depositDate;
    private Status status;
    private String reference;
}
