package com.example.MobilePaluwagan.repository;

import com.example.MobilePaluwagan.entity.SavingsWithdrawApplication;
import com.example.MobilePaluwagan.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SavingsWithdrawApplicationRepo extends JpaRepository<SavingsWithdrawApplication, Long> {

    boolean existsByUserIdAndStatus(Long userId,Status status);
    SavingsWithdrawApplication findByUserIdAndStatus(Long userId,Status status);

    SavingsWithdrawApplication findByUserId(Long userId);

    boolean existsByUserId(Long userId);
    Optional<SavingsWithdrawApplication> findBySavingsId(String savingsId);

}
