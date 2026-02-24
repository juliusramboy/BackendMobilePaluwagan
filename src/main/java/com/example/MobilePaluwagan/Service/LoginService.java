package com.example.MobilePaluwagan.Service;

import com.example.MobilePaluwagan.DTOs.Request.LoginRequest;
import com.example.MobilePaluwagan.DTOs.Response.LoginResponse;
import com.example.MobilePaluwagan.Entity.Token;
import com.example.MobilePaluwagan.Entity.User;
import com.example.MobilePaluwagan.Entity.UserVerification;
import com.example.MobilePaluwagan.Repository.TokenRepository;
import com.example.MobilePaluwagan.Repository.UserRepo;
import com.example.MobilePaluwagan.Repository.VerificationRepo;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoginService {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private VerificationRepo verificationRepo;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private TokenRepository tokenRepository;



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
                User user = userRepo.findByEmail(request.getEmail());
                if (user == null){
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED) .body(new LoginResponse("failed - user not found", null, null));
                }

                UserVerification userVerification = verificationRepo.findByUserId(user.getId());
                if(userVerification == null){
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED) .body(new LoginResponse("failed - OTP not found", null, null));
                }
                if (userVerification.getExpiresAt().isBefore(LocalDateTime.now())){
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED) .body(new LoginResponse("failed - OTP expired", null, null));
                }
                boolean isValidOtp = passwordEncoder.matches(request.getOtp(), userVerification.getOtpHash());
                if (!isValidOtp){
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED) .body(new LoginResponse("failed - invalid OTP", null, null));
                }

                verificationRepo.delete(userVerification);

                String jwt = String.valueOf(jwtService.generateToken(request.getEmail(), user.getId(), user.getRole().getRoleName()));
                Date expiryDate = new Date(System.currentTimeMillis() + JWTService.Expiration_time);

                revokeAllTokenByUser(user);

                saveUserToken(jwt, user);

                LoginResponse response = new LoginResponse(
                        "success",
                        jwt,
                        expiryDate);

                return ResponseEntity.ok(response);

            };



            return ResponseEntity.status(HttpStatus.UNAUTHORIZED) .body(new LoginResponse("failed", null, null));

        } catch (Exception e) {
            System.out.println("=== Login Error ===");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse("failed", null, null));

        }
    }

    private void revokeAllTokenByUser(User user) {
        List<Token> validTokenByUser = tokenRepository.findAllTokenByUser(user.getId());

        if(!validTokenByUser.isEmpty()){
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
