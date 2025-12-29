package com.example.MobilePaluwagan.Service;

import com.example.MobilePaluwagan.DTOs.Request.LoginRequest;
import com.example.MobilePaluwagan.DTOs.Response.LoginResponse;
import com.example.MobilePaluwagan.Entity.User;
import com.example.MobilePaluwagan.Repository.UserRepo;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class LoginService {

    @Autowired
    private UserRepo userRepo;

    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;

    public ResponseEntity<LoginResponse> login(LoginRequest request) {
        try {
            Authentication authentication =
                    authenticationManager.authenticate
                            (new UsernamePasswordAuthenticationToken(
                                    request.getEmail(),
                                    request.getPassword()
                            ));

            if (authentication.isAuthenticated()) {
                String token = String.valueOf(jwtService.generateToken(request.getEmail()));
                Date expiryDate = new Date(System.currentTimeMillis() + JWTService.Expiration_time);

                LoginResponse response = new LoginResponse(
                        "success",
                        token,
                        expiryDate);

                return ResponseEntity.ok(response);

            };

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED) .body(new LoginResponse("failed", null, null));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse("failed", null, null));

        }
    }
}
