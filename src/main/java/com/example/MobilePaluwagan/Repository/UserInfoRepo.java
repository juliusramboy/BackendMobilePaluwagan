package com.example.MobilePaluwagan.Repository;

import com.example.MobilePaluwagan.Entity.User;
import com.example.MobilePaluwagan.Entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserInfoRepo extends JpaRepository<UserInfo, Long> {
    Optional<UserInfo> findByUserId(Long userId);
}

