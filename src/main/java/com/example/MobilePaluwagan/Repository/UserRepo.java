package com.example.MobilePaluwagan.Repository;

import com.example.MobilePaluwagan.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, Long> {
}
