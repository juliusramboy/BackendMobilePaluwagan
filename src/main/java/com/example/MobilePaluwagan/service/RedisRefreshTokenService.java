package com.example.MobilePaluwagan.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisRefreshTokenService {

    private final StringRedisTemplate redisTemplate;

    private static final String REDIS_PREFIX = "refresh_limit:";
    private static final long TIMEOUT_MINUTES = 15;
    private static final int MAX_ALLOWED_USES = 1;

    /**
     * Checks whether the refresh token has exceeded its usage limit (1 use per 15 minutes).
     * Increments the usage count in Redis and sets a 15-minute expiration on the first use.
     *
     * @param refreshToken the refresh token string
     * @return true if rate limited (exceeds 1 use within 15 minutes), false otherwise
     */
    public boolean isRateLimited(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return false;
        }

        String key = REDIS_PREFIX + refreshToken;
        Long currentCount = redisTemplate.opsForValue().increment(key);

        if (currentCount != null && currentCount == 1) {
            redisTemplate.expire(key, TIMEOUT_MINUTES, TimeUnit.MINUTES);
        }

        return currentCount != null && currentCount > MAX_ALLOWED_USES;
    }
}
