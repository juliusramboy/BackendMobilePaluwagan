package com.example.MobilePaluwagan.Repository;

import com.example.MobilePaluwagan.Entity.SavingsWithdrawApplication;
import com.example.MobilePaluwagan.Entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavingsWithdrawApplicationRepo extends JpaRepository<SavingsWithdrawApplication, Long> {

    boolean existsByUserIdAndStatus(Long userId,Status status);

    SavingsWithdrawApplication findByUserId(Long userId);

}
