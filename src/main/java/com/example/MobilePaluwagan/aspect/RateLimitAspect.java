package com.example.MobilePaluwagan.aspect;

import com.example.MobilePaluwagan.annotation.PaymentRateLimited;
import com.example.MobilePaluwagan.exception.RateLimitExceededException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.ai.chat.metadata.RateLimit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class RateLimitAspect {

    private final RedisTemplate<String, String> redisTemplate;

    public RateLimitAspect(RedisTemplate<String, String> redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    @Around("@annotation(paymentRateLimited)")
    public Object enforce(ProceedingJoinPoint joinPoint, PaymentRateLimited rateLimited) throws Throwable{
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        String key = "rate-limit:" + rateLimited.action() + ":" + userId;

        Long attempts = redisTemplate.opsForValue().increment(key);

        if (attempts != null && attempts == 1L){
            redisTemplate.expire(key, rateLimited.windowSeconds(), TimeUnit.SECONDS);
        }

        if (attempts != null && attempts > rateLimited.maxAttempts()) {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            throw new RateLimitExceededException("Too many attempts. Try again in " + ttl + " seconds.");
        }

        return joinPoint.proceed();
    }
}
