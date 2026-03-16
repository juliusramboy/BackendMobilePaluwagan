package com.example.MobilePaluwagan.dto.Request;

import com.example.MobilePaluwagan.entity.Status;
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
