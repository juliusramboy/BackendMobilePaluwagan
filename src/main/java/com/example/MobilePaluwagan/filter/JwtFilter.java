package com.example.MobilePaluwagan.filter;

import com.example.MobilePaluwagan.entity.User;
import com.example.MobilePaluwagan.repository.UserRepo;
import com.example.MobilePaluwagan.service.JWTService;
import com.example.MobilePaluwagan.service.MyUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    JWTService jwtService;

    @Autowired
    ApplicationContext context;

    @Autowired
    UserRepo userRepo;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        // Ignore JWT logic for this specific public path
        return path.equals("/api/admin/loan/user-loan") ||
                path.startsWith("/api/auth/") || 
                path.equals("/api/admin/loan/loan-payments") || 
                path.equals("/api/admin/loan/due-dates") ||
                path.equals("/api/loan/updates") ||
                path.startsWith("/api/mobile/chat/") ||
                path.startsWith("/api/notifications/") ||
                path.startsWith("/api/webhook/") ||
                path.startsWith("/ws/") ||
                path.startsWith("/images/") ||
                path.equals("/error");
    }


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String token = null;
        String username = null;


        //  access token sa cookie — hindi na sa Authorization header
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("accessToken")) {
                    token = cookie.getValue();
                    break;
                }
            }
        }




        // May token sa cookie — subukang i-extract ang username
        if (token != null) {
            try {
                username = jwtService.extractUserName(token);

            } catch (ExpiredJwtException e) {
                // Expired ang access token
                // I-set ang user as offline
                String expiredUsername = e.getClaims().getSubject();
                User user = userRepo.findByEmail(expiredUsername);
                if (user != null && user.isOnline()) {
                    user.setOnline(false);
                    userRepo.save(user);
                }

                // Sabihin sa frontend na expired — kailangan mag-call ng /refresh-token
                sendExpiredTokenResponse(response);
                return; // stop — huwag nang ituloy ang request

            } catch (Exception e) {
                // Invalid token — baka tampered o mali ang format
                sendInvalidTokenResponse(response);
                return; // stop — huwag nang ituloy ang request
            }
        }

        // May username at wala pang authentication — i-authenticate ang user
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = context.getBean(MyUserDetailsService.class)
                    .loadUserByUsername(username);

            if (jwtService.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Ituloy ang request
        filterChain.doFilter(request, response);
    }

    // 401 response — expired ang token, kailangan mag-refresh
    private void sendExpiredTokenResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, String> body = new HashMap<>();
        body.put("status", "token_expired");
        body.put("message", "Your access token has expired.");
        body.put("action", "Call POST /api/auth/refresh-token to get a new access token.");

        new ObjectMapper().writeValue(response.getOutputStream(), body);
    }

    // 401 response — invalid ang token
    private void sendInvalidTokenResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, String> body = new HashMap<>();
        body.put("status", "invalid_token");
        body.put("message", "Your token is invalid. Please log in again.");

        new ObjectMapper().writeValue(response.getOutputStream(), body);
    }
}