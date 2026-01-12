package com.example.MobilePaluwagan.Repository;

import com.example.MobilePaluwagan.Entity.UserSavings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSavingsRepo extends JpaRepository<UserSavings, Long> {

    Optional<UserSavings> findByUserId(long userId);
}
