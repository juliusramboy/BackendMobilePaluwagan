package com.example.MobilePaluwagan.Repository;

import com.example.MobilePaluwagan.DTOs.Request.WeeklyAmortizationSchedule;
import com.example.MobilePaluwagan.Entity.DueDateSchedule;
import com.example.MobilePaluwagan.Entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DueDateScheduleRepository extends JpaRepository<DueDateSchedule, Long> {

    Optional<DueDateSchedule> findFirstByApplicationIdAndStatusOrderByDueDateAsc(Long applicationId, Status status);
    List<DueDateSchedule> findByApplicationIdAndStatus(Long applicationId, Status status);
    List<DueDateSchedule> findByApplicationId(Long applicationId);

    long countByApplicationIdAndStatus(Long applicationId, Status status);

    @Query("SELECT d FROM DueDateSchedule d WHERE d.applicationId = :applicationId " +
            "AND (d.status = 'PENDING' OR d.status = 'PARTIAL') " +
            "ORDER BY " +
            "CASE WHEN d.status = 'PENDING' THEN 1 ELSE 2 END, " + // PENDING first, PARTIAL last
            "d.dueDate ASC LIMIT 1")
    Optional<DueDateSchedule> findFirstPendingOrPartial(@Param("applicationId") Long applicationId);

    @Query("SELECT COUNT(d) FROM DueDateSchedule d WHERE d.applicationId = :applicationId " +
            "AND (d.status = 'PENDING' OR d.status = 'PARTIAL')")
    long countPendingOrPartial(@Param("applicationId") Long applicationId);
}
