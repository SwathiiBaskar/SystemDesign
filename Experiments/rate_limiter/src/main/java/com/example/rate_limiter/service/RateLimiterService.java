package com.example.rate_limiter.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class RateLimiterService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final DefaultRedisScript<List> rateLimiterScript;

    private final int bucketCapacity = 10;
    private final double refillRate = 2.0;

    public RateLimiterService(
            RedisTemplate<String, Object> redisTemplate,
            DefaultRedisScript<List> rateLimiterScript) {

        this.redisTemplate = redisTemplate;
        this.rateLimiterScript = rateLimiterScript;
    }

    public boolean isAllowed(String clientId) {

        String key = "rate_limit:" + clientId;

        long currentTime = System.currentTimeMillis();

        List<Long> result = redisTemplate.execute(
                rateLimiterScript,
                Collections.singletonList(key),
                String.valueOf(currentTime),
                String.valueOf(bucketCapacity),
                String.valueOf(refillRate)
        );

        return result != null && result.get(0) == 1;
    }

    public int getBucketCapacity() {
        return bucketCapacity;
    }

    public double getRefillRate() {
        return refillRate;
    }
}