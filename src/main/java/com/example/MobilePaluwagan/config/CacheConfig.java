package com.example.MobilePaluwagan.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        // Configure Jackson Serializer that preserves type info for accurate JSON deserialization
        ObjectMapper cacheObjectMapper = objectMapper.copy();
        cacheObjectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(cacheObjectMapper);

        // Default Config: 10 mins TTL, String keys, JSON values, skip caching nulls
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        // Custom Cache Configurations
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // 30 min cache for credentials and profiles
        cacheConfigurations.put("userProfile", defaultCacheConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("userDetails", defaultCacheConfig.entryTtl(Duration.ofMinutes(30)));

        // 5 min cache for summaries and transaction dashboards
        cacheConfigurations.put("userLoanSummary", defaultCacheConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("userSavingsSummary", defaultCacheConfig.entryTtl(Duration.ofMinutes(5)));
        
        // 10 min cache for notification lists
        cacheConfigurations.put("userNotifications", defaultCacheConfig.entryTtl(Duration.ofMinutes(10)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
