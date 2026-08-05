package com.example.MobilePaluwagan.aspect;

import com.example.MobilePaluwagan.dto.Response.ApiResponse;
import com.example.MobilePaluwagan.entity.UserPrinciple;
import com.example.MobilePaluwagan.service.RedisIdempotencyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        if (args == null || args.length == 0) {
            return joinPoint.proceed();
        }
        Object requestPayload = args[0];

        // Serialize payload to JSON
        String rawData = objectMapper.writeValueAsString(requestPayload);

        // Append current user ID to prevent cross-user collisions
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserPrinciple) {
            UserPrinciple principal = (UserPrinciple) authentication.getPrincipal();
            rawData = "user-" + principal.userId() + ":" + rawData;
        }

        // Generate MD5 fingerprint
        String fingerprint = DigestUtils.md5DigestAsHex(rawData.getBytes(StandardCharsets.UTF_8));

        // Acquire lock for 1 minute
        boolean isNewRequest = redisIdempotencyService.acquireLock(fingerprint, 1);

        if (!isNewRequest) {
            ApiResponse<?> apiResponse = new ApiResponse<>(false, "Duplicate payment caught. payment already processing...", null);

            // Determine the return type of the method
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Class<?> returnType = signature.getReturnType();

            if (ResponseEntity.class.isAssignableFrom(returnType)) {
                return ResponseEntity.ok(apiResponse);
            } else {
                return apiResponse;
            }
        }

        return joinPoint.proceed();
    }
}
