package com.example.MobilePaluwagan.Service;

import com.example.MobilePaluwagan.Entity.Role;
import com.example.MobilePaluwagan.Repository.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JWTService {

    //@Value("${jwt.secret}")
    private static String secretKey = "a9f8b7c6d5e4f3g2h1i0jklmnopqrstuvwxyz123456";

    public static final long Expiration_time = 1000 * 60 * 60 * 24;

    private final TokenRepository tokenRepository;


    public static String generateToken(String email, Long userId, String role) {
        Map<String, Object>  claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + Expiration_time))
                .and()
                .signWith(getKey())
                .compact();

    }

    private static SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimResolver){
        final Claims claims = extraAllClaims(token);
        return claimResolver.apply(claims);
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public Boolean extractHasLoan(String token) {
        return extractClaim(token, claims -> claims.get("hasLoan", Boolean.class));
    }


    private Claims extraAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, UserDetails userdetails){
        final String username = extractUserName(token);

        boolean isValidToken = tokenRepository.findByToken(token)
                .map(t -> !t.isLoggedOut()).orElse(false);

        return(username.equals(userdetails.getUsername()) && !isTokenExpired(token)) && isValidToken;
    }

    private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }


}
