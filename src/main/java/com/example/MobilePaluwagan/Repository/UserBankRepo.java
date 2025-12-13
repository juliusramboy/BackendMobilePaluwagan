package com.example.MobilePaluwagan.Repository;

import com.example.MobilePaluwagan.Entity.UserBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBankRepo extends JpaRepository<UserBank, Long> {
}
