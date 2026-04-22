package com.example.MobilePaluwagan.service;

import com.example.MobilePaluwagan.dto.Response.LoginResponse;
import com.example.MobilePaluwagan.entity.RefreshToken;
import com.example.MobilePaluwagan.entity.Token;
import com.example.MobilePaluwagan.entity.User;
import com.example.MobilePaluwagan.repository.RefreshTokenRepo;
import com.example.MobilePaluwagan.repository.TokenRepository;
import com.example.MobilePaluwagan.repository.UserRepo;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final Long REFRESH_EXPIRATION_MS = 1000L * 60 * 60 * 24 * 7;

    private final RefreshTokenRepo refreshTokenRepo;
    private final TokenRepository tokenRepository;
    private final JWTService jwtService;
    private final UserRepo userRepo;

    @Transactional
    public RefreshToken createFreshToken(User user) {

        refreshTokenRepo.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken.setExpiryDate(Instant.now().plusMillis(REFRESH_EXPIRATION_MS));

        refreshToken.setUser(user);

        return refreshTokenRepo.save(refreshToken);
    }

    public ResponseEntity<?> handleRefreshToken(HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("refresh_token")) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse("No refresh token found. Please log in again."));
        }

        String finalRefreshToken = refreshToken;

        return findByToken(finalRefreshToken)
                .map(this::verifyToken)
                .map(RefreshToken::getUser)
                .map(user -> {

                    revokeAllTokenByUser(user);

                    String newAccessToken = jwtService.generateToken(
                            user.getEmail(),
                            user.getId(),
                            user.getRole().getRoleName()
                    );

                    saveUserToken(newAccessToken, user);

                    // ✅ newAccessToken na — hindi na 'value'
                    ResponseCookie accessCookie = ResponseCookie.from("accessToken", newAccessToken)
                            .httpOnly(true)
                            .secure(true)        // true pag prod
                            .sameSite("None")
                            .path("/")
                            .domain("54.251.224.183")
                            .maxAge(60 * 15) // 24 hours
                            .build();
                    response.addHeader("Set-Cookie", accessCookie.toString());

                    long expiresAt = (System.currentTimeMillis() + JWTService.Expiration_time) / 1000;

                    return ResponseEntity.ok(new LoginResponse(
                            "success",
                            user.getId(),
                            user.getEmail(),
                            user.getRole().getRoleName(),
                            expiresAt
                    ));
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new LoginResponse("Invalid refresh token. Please log in again.")));
    }

    // Helper methods
    private void revokeAllTokenByUser(User user) {
        List<Token> validTokens = tokenRepository.findAllTokenByUser(user.getId());
        if (!validTokens.isEmpty()) {
            validTokens.forEach(t -> t.setLoggedOut(true));
        }
        tokenRepository.saveAll(validTokens);
    }

    private void saveUserToken(String jwt, User user) {
        Token token = new Token();
        token.setToken(jwt);
        token.setLoggedOut(false);
        token.setUser(user);
        tokenRepository.save(token);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepo.findByToken(token);
    }

    public RefreshToken verifyToken(RefreshToken token){
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepo.delete(token);
            throw new RuntimeException("Refresh token expired. Please log in again.");
        }
        return token;
    }

    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepo.deleteByUser(user);
    }
}
