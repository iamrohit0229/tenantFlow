package com.tenantflow.identity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtService jwtService;

    private static final String BLACKLIST_PREFIX = "blacklist:";

    // Called on logout
    // Token stored in Redis until it naturally expires
    public void blacklistToken(String token) {
        long remainingValidity = jwtService.extractRemainingValidity(token);

        if (remainingValidity > 0) {
            String key = BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(
                    key,
                    "blacklisted",
                    remainingValidity,
                    TimeUnit.MILLISECONDS
            );
            log.info("Token blacklisted successfully");
        }
    }

    // Called on every request by JwtAuthFilter
    public boolean isTokenBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
