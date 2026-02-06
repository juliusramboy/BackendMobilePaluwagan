package com.example.MobilePaluwagan.DTOs.Request;

import com.example.MobilePaluwagan.Entity.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminLoanStatus {
    private Long applicationID;
    private Status status;
}
