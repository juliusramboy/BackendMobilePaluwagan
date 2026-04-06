package com.example.MobilePaluwagan.dto.Response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    // Status ng login — "success" or "failed"
    private String status;

    // HINDI na natin isinasama ang token sa body
    // Nasa HTTP-only cookie na siya
    // Pero pinananatili natin para sa backward compatibility
    // (pag may endpoints pa na gumagamit nito)
    private String token;

    // BAGO — User info na kailangan ng frontend
    // Para hindi na kailangan ng jwtDecode sa frontend
    private Long userId;       // ID ng user
    private String email;      // Email ng user
    private String role;       // "ADMIN" or "USER" — para sa role-based routing
    private long expiresAt;    // Unix timestamp — para malaman ng frontend kung kelan mag-eexpire

    // Constructor para sa successful login
    // Ito ang gagamitin sa LoginService
    public LoginResponse(String status, Long userId, String email, String role, long expiresAt) {
        this.status = status;
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.expiresAt = expiresAt;
    }

    // Constructor para sa error responses — status message lang
    public LoginResponse(String status) {
        this.status = status;
    }
}