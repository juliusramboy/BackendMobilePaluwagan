package com.example.MobilePaluwagan.config;

import com.example.MobilePaluwagan.entity.Token;
import com.example.MobilePaluwagan.repository.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CustomLogoutHandler implements LogoutHandler {
    private final TokenRepository tokenRepository;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String authHeader = request.getHeader("Authorization");


        if (authHeader == null && !authHeader.startsWith("Bearer ")) {
            return;
        }
        String token = authHeader.substring(7);

        Token storedToken = tokenRepository.findByToken(token).orElse(null);

        if (storedToken != null){
            storedToken.setLoggedOut(true);
            tokenRepository.save(storedToken);
        }
    }
}
