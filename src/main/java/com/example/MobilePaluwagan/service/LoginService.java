package com.example.MobilePaluwagan.service;

import com.example.MobilePaluwagan.dto.Request.LoginRequest;
import com.example.MobilePaluwagan.dto.Response.LoginResponse;
import com.example.MobilePaluwagan.entity.RefreshToken;
import com.example.MobilePaluwagan.entity.Token;
import com.example.MobilePaluwagan.entity.User;
import com.example.MobilePaluwagan.entity.UserVerification;
import com.example.MobilePaluwagan.repository.TokenRepository;
import com.example.MobilePaluwagan.repository.UserRepo;
import com.example.MobilePaluwagan.repository.VerificationRepo;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;  // ✅ ResponseCookie na, hindi Cookie
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepo userRepo;
    private final VerificationRepo verificationRepo;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;
    private final RefreshTokenService refreshTokenService;
    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;

    public ResponseEntity<LoginResponse> login(LoginRequest request, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            if (authentication.isAuthenticated()) {
                User user = userRepo.findByEmail(request.getEmail());
                if (user == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new LoginResponse("failed - user not found"));
                }

                UserVerification userVerification = verificationRepo.findByUserId(user.getId());
                if (userVerification == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new LoginResponse("failed - OTP not found"));
                }
                if (userVerification.getExpiresAt().isBefore(LocalDateTime.now())) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new LoginResponse("failed - OTP expired"));
                }

                boolean isValidOtp = passwordEncoder.matches(request.getOtp(), userVerification.getOtpHash());
                if (!isValidOtp) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new LoginResponse("failed - invalid OTP"));
                }

                verificationRepo.delete(userVerification);
                user.setOnline(true);
                userRepo.save(user);

                String jwt = jwtService.generateToken(
                        request.getEmail(),
                        user.getId(),
                        user.getRole().getRoleName()
                );

                revokeAllTokenByUser(user);
                saveUserToken(jwt, user);

                RefreshToken refreshToken = refreshTokenService.createFreshToken(user);

                // ✅ Access token cookie — ResponseCookie para may SameSite
                ResponseCookie accessCookie = ResponseCookie.from("accessToken", jwt)
                        .httpOnly(true)
                        .secure(false)          // false habang local, true pag prod
                        .path("/")
                        .maxAge(60 * 60 * 24)   // 24 hours
                        .sameSite("Lax")        // ✅ kailangan para gumana sa browser
                        .build();
                response.addHeader("Set-Cookie", accessCookie.toString());

                // ✅ Refresh token cookie — ResponseCookie para may SameSite
                ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken.getToken())
                        .httpOnly(true)
                        .secure(false)               // false habang local, true pag prod
                        .path("/")
                        .maxAge(7 * 24 * 60 * 60)   // 7 days
                        .sameSite("Lax")             // ✅ kailangan para gumana sa browser
                        .build();
                response.addHeader("Set-Cookie", refreshCookie.toString());

                long expiresAt = (System.currentTimeMillis() + JWTService.Expiration_time) / 1000;

                return ResponseEntity.ok(new LoginResponse(
                        "success",
                        user.getId(),
                        user.getEmail(),
                        user.getRole().getRoleName(),
                        expiresAt
                ));
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse("failed"));

        } catch (Exception e) {
            System.out.println("=== Login Error ===");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse("failed"));
        }
    }

    private void revokeAllTokenByUser(User user) {
        List<Token> validTokenByUser = tokenRepository.findAllTokenByUser(user.getId());
        if (!validTokenByUser.isEmpty()) {
            validTokenByUser.forEach(token -> token.setLoggedOut(true));
        }
        tokenRepository.saveAll(validTokenByUser);
    }

    private void saveUserToken(String jwt, User user) {
        Token token = new Token();
        token.setToken(jwt);
        token.setLoggedOut(false);
        token.setUser(user);
        tokenRepository.save(token);
    }
}