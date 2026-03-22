package com.example.MobilePaluwagan.config;

import com.example.MobilePaluwagan.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminStatusTracker {

    private final UserRepo userRepo;

    public boolean isAnyAdminOnline() {
        return userRepo.existsByIsOnlineTrueAndRoleRoleName("ROLE_ADMIN");
    }
}
