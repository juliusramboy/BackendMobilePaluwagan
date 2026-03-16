package com.example.MobilePaluwagan.aspect;

import com.example.MobilePaluwagan.dto.Response.ApiResponse;
import com.example.MobilePaluwagan.service.RedisIdempotencyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

@Aspect
@Component
public class IdempotencyAspect {

    private final RedisIdempotencyService redisIdempotencyService;
    private final ObjectMapper objectMapper;

    public IdempotencyAspect(RedisIdempotencyService redisIdempotencyService, ObjectMapper objectMapper) {
        this.redisIdempotencyService = redisIdempotencyService;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(com.example.MobilePaluwagan.annotation.Idempotent)")
    public Object checkIdempotent(ProceedingJoinPoint joinPoint) throws Throwable {

        Object[] args = joinPoint.getArgs();
        Object requestPayload = args[0];


        String rawData = objectMapper.writeValueAsString(requestPayload);
        String fingerprint = DigestUtils.md5DigestAsHex(rawData.getBytes(StandardCharsets.UTF_8));

        boolean isNewRequest = redisIdempotencyService.acquireLock(fingerprint, 2);

        if (!isNewRequest) {
            return new ApiResponse<>(false, "Duplicate payment caught. payment already processing...", null);
        }

        return joinPoint.proceed();
    }
}
