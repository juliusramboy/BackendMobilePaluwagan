package com.example.MobilePaluwagan.service;

import com.example.MobilePaluwagan.exception.InvalidTransactionTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TransactionTokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final long TOKEN_TTL_SECONDS = 120;

    public String issueToken(String userId){
        String token = UUID.randomUUID().toString();
        String key = "txn-token:" + token;

        redisTemplate.opsForValue().set(key, userId, TOKEN_TTL_SECONDS, TimeUnit.SECONDS);

        return token;
    }

    public String validateAndConsume(String token){
        String key = "txn-token:" + token;
        String userId = redisTemplate.opsForValue().get(key);

        if (userId == null) {
            throw new InvalidTransactionTokenException("Token invalid or expired.");
        }

        redisTemplate.delete(key);
        return userId;
    }
}
