package com.example.MobilePaluwagan.dto.Request;

import com.example.MobilePaluwagan.entity.Status;
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
public class WeeklyAmortizationSchedule {

    private Long applicationId;
    private int week;
    private LocalDate dueDate;
    private BigDecimal payment;
    private Status status;

}