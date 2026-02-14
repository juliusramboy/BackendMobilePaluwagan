package com.example.MobilePaluwagan.Repository;

import com.example.MobilePaluwagan.Entity.SavingsApplication;
import com.example.MobilePaluwagan.Entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavingsApplicationRepo extends JpaRepository<SavingsApplication, Long> {
    boolean existsByUserIdAndStatusIn(Long userId, List<Status> statuses);
}
