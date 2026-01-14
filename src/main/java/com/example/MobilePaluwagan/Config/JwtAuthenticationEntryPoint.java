package com.example.MobilePaluwagan.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, Object> errorDetails = new HashMap<>();

        // Check if token was expired or invalid
        if (request.getAttribute("expired") != null) {
            errorDetails.put("error", "EXPIRED_TOKEN");
            errorDetails.put("message", request.getAttribute("expired"));
        } else if (request.getAttribute("invalid") != null) {
            errorDetails.put("error", "INVALID_TOKEN");
            errorDetails.put("message", request.getAttribute("invalid"));
        } else {
            errorDetails.put("error", "UNAUTHORIZED");
            errorDetails.put("message", "Authentication required");
        }

        errorDetails.put("timestamp", LocalDateTime.now().toString());
        errorDetails.put("path", request.getRequestURI());

        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(errorDetails));
    }
}