package com.example.MobilePaluwagan.Repository;

import com.example.MobilePaluwagan.Entity.UserBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserBankRepo extends JpaRepository<UserBank, Long> {

    Optional<UserBank> findByAccountBalance(Long accountBalance);

    Optional<UserBank> findByTargetAmount(Long targetAmount);

    Optional<UserBank> findByUserId(Long userId);
}
