package com.zerobase.homemate.badge.service;

import com.zerobase.homemate.entity.enums.Space;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserBadgeStatsService {

    private final StringRedisTemplate redisTemplate;

    // Key Format
    private static final String STATS_KEY_FORMAT = "user:stats:%d";
    private static final String SPACE_KEY_FORMAT = "user:stats:%d:space";
    private static final String TITLE_KEY_FORMAT = "user:stats:%d:title";

    // Field Format
    private static final String FIELD_TOTAL_COMPLETED = "total_completed";
    private static final String FIELD_TOTAL_REGISTERED = "total_registered";
    private static final String FIELD_MISSION_COUNT = "mission_count";
    private static final String FIELD_LAST_UPDATED = "last_updated";

    // Increment Methods
    public long incrementTotalCompleted(Long userId){
        String key =  String.format(STATS_KEY_FORMAT, userId);
        return redisTemplate.opsForHash().increment(key, FIELD_TOTAL_COMPLETED, 1);
    }

    public long incrementTotalRegistered(Long userId){
        String key =  String.format(STATS_KEY_FORMAT, userId);
        return redisTemplate.opsForHash().increment(key, FIELD_TOTAL_REGISTERED, 1);
    }

    public long incrementMissionCount(Long userId){
        String key =  String.format(STATS_KEY_FORMAT, userId);
        return redisTemplate.opsForHash().increment(key, FIELD_MISSION_COUNT, 1);
    }

    public long incrementSpaceCount(Long userId, Space space){
        String key =  String.format(SPACE_KEY_FORMAT, userId);
        return redisTemplate.opsForHash().increment(key, space, 1);
    }

    public long incrementTitleCount(Long userId, String title){
        String key = String.format(TITLE_KEY_FORMAT, userId);
        return redisTemplate.opsForHash().increment(key, title, 1);
    }

    // get Count Method
    public long getTotalCompletedCount(Long userId){
        Object v = redisTemplate.opsForHash().get(String.format(STATS_KEY_FORMAT, userId), FIELD_TOTAL_COMPLETED);
        return parseLongSafe(v);
    }

    public long getTotalRegisteredCount(Long userId){
        Object v = redisTemplate.opsForHash().get(String.format(STATS_KEY_FORMAT, userId), FIELD_TOTAL_REGISTERED);
        return parseLongSafe(v);
    }

    public long getTotalMissionCount(Long userId){
        Object v = redisTemplate.opsForHash().get(String.format(STATS_KEY_FORMAT, userId), FIELD_MISSION_COUNT);
        return parseLongSafe(v);
    }

    public long getSpaceCount(Long userId, Space space){
        Object v = redisTemplate.opsForHash().get(String.format(SPACE_KEY_FORMAT, userId), space);
        return parseLongSafe(v);
    }

    public long getTitleCount(Long userId, String title){
        Object v = redisTemplate.opsForHash().get(String.format(TITLE_KEY_FORMAT, userId), title);
        return parseLongSafe(v);
    }


    // Sub Method for these!

    private long parseLongSafe(Object v) {
        if(v == null) return 0L;
        try{
            return Long.parseLong(String.valueOf(v));
        } catch(NumberFormatException e){
            return 0L;
        }

    }

    private void setLastUpdated(Long userId, long epochSeconds) {
        String key = String.format(STATS_KEY_FORMAT, userId);
        try {
            redisTemplate.opsForHash().put(key, FIELD_LAST_UPDATED, String.valueOf(epochSeconds));
        } catch (Exception ignored) {}
    }

}
