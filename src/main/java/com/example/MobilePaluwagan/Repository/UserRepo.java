package com.example.MobilePaluwagan.Repository;

import com.example.MobilePaluwagan.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

    User findByEmail(String email);
    User findByIsActive(Boolean isActive);

    @Query("SELECT COUNT(u) FROM User u WHERE u.hasSavingsAccount = true")
    int countAllSavingsMembers();



}
