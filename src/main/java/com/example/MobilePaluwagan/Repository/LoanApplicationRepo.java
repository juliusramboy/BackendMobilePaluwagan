package com.example.MobilePaluwagan.Repository;

import com.example.MobilePaluwagan.Entity.LoanApplication;
import com.example.MobilePaluwagan.Entity.Role;
import com.example.MobilePaluwagan.Entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoanApplicationRepo extends JpaRepository<LoanApplication, Long> {

    Optional<LoanApplication> findByUserId(Long userId);

    List<LoanApplication> findAllByUserId(Long userId);

    List<LoanApplication> findAllByUserIdAndStatus(Long userId, Status status);
}
