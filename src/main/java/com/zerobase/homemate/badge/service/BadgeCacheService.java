package com.zerobase.homemate.badge.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobase.homemate.badge.BadgeProgressResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BadgeCacheService {

    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;



    private static final String KEY_PREFIX = "badge:closest:";
    private static final Duration TTL = Duration.ofSeconds(30); // 캐시 생명주기 30초

    private String key(Long userId){
        return KEY_PREFIX + userId;
    }

    public List<BadgeProgressResponse> getCachedClosestBadges(Long userId){
        String redisKey = key(userId);

        log.info("[CACHE][READ] Try read key={} (userId={})", redisKey, userId);

        String json = redisTemplate.opsForValue().get(redisKey);

        if(json == null){
            log.info("[CACHE][MISS] key={} (userId={})", redisKey, userId);
            return null;
        }

        try {
            List<BadgeProgressResponse> result = objectMapper.readValue(
                    json,
                    new TypeReference<>() {
                    }
            );

            log.info("[CACHE][HIT] key={} → size={}", redisKey, result.size());
            return result;

        } catch (Exception e) {
            log.error("[CACHE][ERROR] Failed to parse cache for key={} → evicted", redisKey, e);
            redisTemplate.delete(redisKey);
            return null;
        }
    }

    public void cacheClosestBadges(Long userId, List<BadgeProgressResponse> badges){
        String redisKey = key(userId);

        try {
            String json = objectMapper.writeValueAsString(badges);

            redisTemplate.opsForValue().set(redisKey, json, TTL);

            log.info("[CACHE][WRITE] Saved {} badges to key={} (TTL={}s)",
                    badges.size(), redisKey, TTL.getSeconds());

        } catch (Exception e) {
            log.warn("[CACHE][WRITE-FAIL] Failed to cache key={} (userId={})",
                    redisKey, userId, e);
        }
    }

    public void evictClosestBadges(Long userId){
        String redisKey = key(userId);

        try {
            redisTemplate.delete(redisKey);
            log.info("[CACHE][EVICT] key={} removed (userId={})", redisKey, userId);

        } catch (Exception e) {
            log.warn("[CACHE][EVICT-FAIL] Failed to delete key={} (userId={})",
                    redisKey, userId, e);
        }
    }
}
