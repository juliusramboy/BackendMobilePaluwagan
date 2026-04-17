package com.example.MobilePaluwagan.dto.Response;

import com.example.MobilePaluwagan.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserListDueDates {
    private LocalDate dueDate;
    private BigDecimal payment;
    private Status status;
}
