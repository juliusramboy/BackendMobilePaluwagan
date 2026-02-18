package com.example.MobilePaluwagan.Repository;

import com.example.MobilePaluwagan.Entity.Status;
import com.example.MobilePaluwagan.Entity.UserSavings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Repository
public interface UserSavingsRepo extends JpaRepository<UserSavings, Long> {

    List<UserSavings> findByUserId(long userId);
    Long countByUserId(long userId);
    boolean existsByUserIdAndStatus(long userId, Status status);

    @Query("SELECT e FROM UserSavings e ORDER BY e.id desc LIMIT 1")
    Optional<UserSavings> findLastRef();
}
