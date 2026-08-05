package com.example.MobilePaluwagan.service;

import com.example.MobilePaluwagan.entity.User;
import com.example.MobilePaluwagan.exception.AccountLockedException;
import com.example.MobilePaluwagan.exception.InvalidPinException;
import com.example.MobilePaluwagan.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionAuthService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final PinLockoutService pinLockoutService;

    public void verifyPin(Long userId, String rawPassword){
        if (pinLockoutService.isLocked(userId)){
            long remaining = pinLockoutService.getLockRemainingSeconds(userId);
            throw new AccountLockedException("Too many failed attempts. tye again " + remaining + " seconds");
        }

        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found: " + userId));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())){
            pinLockoutService.recordFailedAttempt(userId);
            throw new InvalidPinException("incorrect password");
        }
    }

}
