package com.example.MobilePaluwagan.config;

import com.example.MobilePaluwagan.entity.Token;
import com.example.MobilePaluwagan.entity.User;
import com.example.MobilePaluwagan.repository.TokenRepository;
import com.example.MobilePaluwagan.repository.UserRepo;
import com.example.MobilePaluwagan.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CustomLogoutHandler implements LogoutHandler {

    private final TokenRepository tokenRepository;
    private final UserRepo userRepo;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        // bumabasa ng access token sa cookie
        String accessToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("accessToken")) {
                    accessToken = cookie.getValue();
                    break;
                }
            }
        }

        if (accessToken == null) {
            return;
        }


        Token storedToken = tokenRepository.findByToken(accessToken).orElse(null);

        if (storedToken != null) {
            // I-set ang user as offline
            User user = storedToken.getUser();
            user.setOnline(false);
            userRepo.save(user);

            // I-revoke ang access token
            storedToken.setLoggedOut(true);
            tokenRepository.save(storedToken);

            // I-delete ang refresh token sa DB
            refreshTokenService.deleteByUser(user);
        }

        // I-delete ang accessToken cookie — ResponseCookie na, maxAge(0) = delete
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(false)    // true pag prod
                .path("/")
                .maxAge(0)        // 0 = delete agad
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", accessCookie.toString());

        //  I-delete ang refresh_token cookie — ResponseCookie na, maxAge(0) = delete
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)    // true pag prod
                .path("/")
                .maxAge(0)        // 0 = delete agad
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", refreshCookie.toString());
    }
}