package com.example.MobilePaluwagan.Repository;

import com.example.MobilePaluwagan.DTOs.Response.LoanApplicantsAdmin;
import com.example.MobilePaluwagan.Entity.LoanApplication;
import com.example.MobilePaluwagan.Entity.Role;
import com.example.MobilePaluwagan.Entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

public interface LoanApplicationRepo extends JpaRepository<LoanApplication, Long> {

    Optional<LoanApplication> findByUserId(Long userId);

    List<LoanApplication> findAllByUserId(Long userId);

    List<LoanApplication> findAllByUserIdAndStatus(Long userId, Status status);

    boolean existsByUserIdAndStatusIn(Long userId, List<Status> statuses);
}
