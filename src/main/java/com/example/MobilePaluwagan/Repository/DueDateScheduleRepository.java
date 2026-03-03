package com.example.MobilePaluwagan.Repository;

import com.example.MobilePaluwagan.DTOs.Request.WeeklyAmortizationSchedule;
import com.example.MobilePaluwagan.Entity.DueDateSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DueDateScheduleRepository extends JpaRepository<DueDateSchedule, Long> {
}
