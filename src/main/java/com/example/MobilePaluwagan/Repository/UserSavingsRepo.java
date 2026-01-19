package com.example.MobilePaluwagan.Repository;

import com.example.MobilePaluwagan.Entity.Status;
import com.example.MobilePaluwagan.Entity.UserSavings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSavingsRepo extends JpaRepository<UserSavings, Long> {

    List<UserSavings> findByUserId(long userId);
    Long countByUserId(long userId);
    boolean existsByUserIdAndStatus(long userId, Status status);
}
