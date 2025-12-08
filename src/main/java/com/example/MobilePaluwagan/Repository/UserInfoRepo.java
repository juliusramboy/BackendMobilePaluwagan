package com.example.MobilePaluwagan.Repository;

import com.example.MobilePaluwagan.Entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInfoRepo extends JpaRepository<UserInfo, Long> {
}
