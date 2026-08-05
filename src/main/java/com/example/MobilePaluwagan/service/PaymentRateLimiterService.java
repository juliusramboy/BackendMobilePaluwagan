package com.example.MobilePaluwagan.service;

import com.example.MobilePaluwagan.exception.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PaymentRateLimiterService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_SECONDS = 900;


    public void checkPaymentRateLimit(Long userId, String action){
        String key = "payment-rate-limit" + action + ":" + userId;

        Long attempts = redisTemplate.opsForValue().increment(key);

        if(attempts != null && attempts == 1L){
            redisTemplate.expire(key, WINDOW_SECONDS, TimeUnit.SECONDS);
        }

        if(attempts != null && attempts > MAX_ATTEMPTS){
            long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            throw new RateLimitExceededException(
                    "Too many attempts. try again in " + ttl + " seconds."
            );
        }
    }

    public void resetPaymentRateLimiter(Long userId, String action){
        String key = "payment-rate-limit" + action + ":" + userId;
        redisTemplate.delete(key);
    }

}
