package com.example.MobilePaluwagan.DTOs.Request;

import com.example.MobilePaluwagan.Entity.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminSavingsStatus {
    private String savingsId;
    private String reference;
    private Status status;
}
