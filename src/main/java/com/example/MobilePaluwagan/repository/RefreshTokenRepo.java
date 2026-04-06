package com.example.MobilePaluwagan.repository;

import com.example.MobilePaluwagan.entity.RefreshToken;
import com.example.MobilePaluwagan.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User token);
}
