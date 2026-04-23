package com.example.MobilePaluwagan.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimiterInterceptor implements HandlerInterceptor {

    private final Cache<String, RateLimiter> limiters = CacheBuilder.newBuilder()
            .expireAfterAccess(2, TimeUnit.MINUTES)
            .maximumSize(500)
            .build();

    private RateLimiter getLimiter(String ip) {
        try {
            return limiters.get(ip, () -> RateLimiter.create(10.0));
        } catch (ExecutionException e) {
            return RateLimiter.create(10.0);
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        String clientIp;

        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            clientIp = xForwardedFor.split(",")[0].trim();
        }else {
            clientIp = request.getRemoteAddr();
        }

        if (!getLimiter(clientIp).tryAcquire()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Too many requests, please try again later.\"}");
            return false;
        }
        return true;
    }
}