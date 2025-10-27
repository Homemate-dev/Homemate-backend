package com.zerobase.homemate.badge.service;

import com.zerobase.homemate.entity.enums.Space;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserBadgeStatsService {

    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX_SPACE = "user:chore:space";
    private static final String PREFIX_TITLE = "user:chore:title";
    private static final String PREFIX_ALL = "user:chore:all";
    private static final String PREFIX_MISSION = "user:chore:mission";


    private String buildKey(String prefix, Long userId) {
        return String.format("%s:%d", prefix, userId);
    }

    private String buildKey(String prefix, Long userId, Object subKey) {
        String subKeyStr;

        // Enum은 name()으로 고정 변환 (toString 오버라이드 영향 방지)
        if (subKey instanceof Enum<?>) {
            subKeyStr = ((Enum<?>) subKey).name();
        } else {
            subKeyStr = subKey.toString();
        }

        return String.format("%s:%d:%s", prefix, userId, subKeyStr);
    }

    /* ===============================
       ✅ Count 증가 로직
       =============================== */

    // (1) 아무 집안일 완료 시
    public void incrementCount(Long userId) {
        redisTemplate.opsForValue().increment(buildKey(PREFIX_ALL, userId), 1);
    }

    // 공간 카테고리에 속한 집안일을 완료하면 그 카테고리에 속한 횟수가 늘어난다.
    public void incrementSpaceCount(Long userId, Space space) {
        redisTemplate.opsForValue().increment(buildKey(PREFIX_SPACE, userId, space), 1);
    }

    // 특정 이름을 가진 집안일은 완료 시 카운팅이 된다.
    public void incrementTitleCount(Long userId, String title) {
        redisTemplate.opsForValue().increment(buildKey(PREFIX_TITLE, userId, title), 1);
    }

    // 미션 완료 시 미션 카운팅이 된다.
    public void incrementMissionCount(Long userId) {
        redisTemplate.opsForValue().increment(buildKey(PREFIX_MISSION, userId), 1);
    }


    // Redis 횟수 조회 로직
    public long getCount(Long userId) {
        return getLongValue(buildKey(PREFIX_ALL, userId));
    }

    public long getSpaceCount(Long userId, Space space) {
        return getLongValue(buildKey(PREFIX_SPACE, userId, space));
    }

    public long getTitleCount(Long userId, String title) {
        return getLongValue(buildKey(PREFIX_TITLE, userId, title));
    }

    public long getMissionCount(Long userId) {
        return getLongValue(buildKey(PREFIX_MISSION, userId));
    }


    private long getLongValue(String key) {
        String val = redisTemplate.opsForValue().get(key);
        return val != null ? Long.parseLong(val) : 0L;
    }


}
