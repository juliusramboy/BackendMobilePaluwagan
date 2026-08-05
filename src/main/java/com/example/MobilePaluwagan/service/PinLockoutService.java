package com.example.MobilePaluwagan.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class PinLockoutService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final int MAX_ATTEMPTS = 3;
    private static final long LOCK_DURATION_SECONDS = 900;

    public PinLockoutService(RedisTemplate<String, String> redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    public boolean isLocked(Long userId){
        String lockKey = "pin-lock" + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
    }

    public void recordFailedAttempt(Long userId){
        String attemptsKey = "pin-attempt:" + userId;

        Long attempts = redisTemplate.opsForValue().increment(attemptsKey);
        if (attempts != null && attempts == 1L){
            redisTemplate.expire(attemptsKey, LOCK_DURATION_SECONDS, TimeUnit.SECONDS);
        }

        if (attempts != null && attempts >= MAX_ATTEMPTS){
            String lockKey = "pin-lock:" + userId;
            redisTemplate.opsForValue().set(lockKey, "locked", LOCK_DURATION_SECONDS, TimeUnit.SECONDS);
        }
    }

    public void resetAttempts(Long userId){
        redisTemplate.delete("pin-attempts:" + userId);
        redisTemplate.delete("pin-lock:" + userId);
    }

    public long getLockRemainingSeconds(Long userId) {
        Long ttl = redisTemplate.getExpire("pin-lock:" + userId, TimeUnit.SECONDS);
        return ttl != null ? ttl : 0;
    }
}
