package com.example.MobilePaluwagan.Repository;

import com.example.MobilePaluwagan.Entity.SavingsWithdrawApplication;
import com.example.MobilePaluwagan.Entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavingsWithdrawApplicationRepo extends JpaRepository<SavingsWithdrawApplication, Long> {

    boolean existsByUserIdAndStatus(Long userId,Status status);

    SavingsWithdrawApplication findByUserId(Long userId);
    Optional<SavingsWithdrawApplication> findBySavingsId(String savingsId);

}
