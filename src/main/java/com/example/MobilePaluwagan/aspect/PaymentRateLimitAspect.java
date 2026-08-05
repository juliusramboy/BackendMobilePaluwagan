package com.example.MobilePaluwagan.aspect;

import com.example.MobilePaluwagan.annotation.PaymentRateLimited;
import com.example.MobilePaluwagan.exception.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
public class PaymentRateLimitAspect {

    private final RedisTemplate<String, String> redisTemplate;


    @Around("@annotation(rateLimited)")
    public Object enforce(ProceedingJoinPoint joinPoint, PaymentRateLimited rateLimited) throws Throwable{
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        String key = "payment-rate-limit" + rateLimited.action() + ":" + userId;

        Long attempts = redisTemplate.opsForValue().increment(key);
        if (attempts == 1L){
            redisTemplate.expire(key, rateLimited.windowSeconds(), TimeUnit.SECONDS);
        }
        if (attempts > rateLimited.maxAttempts()){
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            throw new RateLimitExceededException("Too many attempts. Try again in " + ttl + "s.");
        }

        return joinPoint.proceed();
    }

}
