package com.example.MobilePaluwagan.aspect;

import com.example.MobilePaluwagan.dto.Response.ApiResponse;
import com.example.MobilePaluwagan.service.RedisIdempotencyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
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
        Object requestPayload = getRequestBody(joinPoint);
        if (requestPayload == null) {
            return joinPoint.proceed();
        }

        // Contextual Fingerprint: Include method name & username to prevent collisions
        String rawData = objectMapper.writeValueAsString(requestPayload);
        String methodName = joinPoint.getSignature().toShortString();
        String username = "anonymous";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            username = auth.getName();
        }

        String fingerprintBase = methodName + ":" + username + ":" + rawData;
        String fingerprint = DigestUtils.md5DigestAsHex(fingerprintBase.getBytes(StandardCharsets.UTF_8));

        // Acquire lock
        boolean isNewRequest = redisIdempotencyService.acquireLock(fingerprint, 2);

        if (!isNewRequest) {
            return buildErrorResponse(joinPoint);
        }

        try {
            return joinPoint.proceed();
        } catch (Throwable t) {
            // Safety Valve: Release lock if execution fails so user can retry
            redisIdempotencyService.releaseLock(fingerprint);
            throw t;
        }
    }

    private Object getRequestBody(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (Annotation annotation : parameterAnnotations[i]) {
                if (annotation instanceof RequestBody) {
                    return args[i];
                }
            }
        }
        return args.length > 0 ? args[0] : null;
    }

    private Object buildErrorResponse(ProceedingJoinPoint joinPoint) {
        Class<?> returnType = ((MethodSignature) joinPoint.getSignature()).getReturnType();
        String msg = "Duplicate payment caught. payment already processing...";
        
        if (ResponseEntity.class.isAssignableFrom(returnType)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(false, msg, null));
        }
        return new ApiResponse<>(false, msg, null);
    }
}
