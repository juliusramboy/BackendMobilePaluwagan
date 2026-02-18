package com.example.MobilePaluwagan.Repository;

import com.example.MobilePaluwagan.Entity.UserVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationRepo extends JpaRepository<UserVerification, Long> {
    UserVerification findByUserId(Long userId);
}
