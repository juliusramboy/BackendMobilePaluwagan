package com.example.MobilePaluwagan.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisIdempotencyService {

    private final StringRedisTemplate redisTemplate;

    public RedisIdempotencyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean acquireLock(String fingerprint, long timeoutMinutes) {
        Boolean isNewKey = redisTemplate.opsForValue().setIfAbsent(
                "idempotency" + fingerprint,
                "locked",
                timeoutMinutes,
                TimeUnit.MINUTES
        );

        return isNewKey != null && isNewKey;
    }
}
