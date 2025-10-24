package com.zerobase.homemate.badge.service;

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


    // 공간별, 집안일별이 아닌 아무 집안일이든 올라가는 횟수 증가
    public void incrementCount(Long userId){
        String key = String.format("%s:%d", PREFIX_ALL, userId);
        redisTemplate.opsForValue().increment(key, 1);
    }

    // 공간별 집안일 완료 횟수 증가
    public void incrementSpaceCount(Long userId, String space) {
        String key = String.format("%s:%d:%s", PREFIX_SPACE, userId, space);
        redisTemplate.opsForValue().increment(key, 1);
    }

    // 특정 집안일별 완료 횟수 증가
    public void incrementTitleCount(Long userId, String title) {
        String key = String.format("%s:%d:%s", PREFIX_TITLE, userId, title);
        redisTemplate.opsForValue().increment(key, 1);
    }

    // 공간별, 집안일별이 아닌 아무 집안일이든 올라가는 횟수 조회
    public long getCount(Long userId){
        String key = String.format("%s:%d", PREFIX_ALL, userId);
        String val = redisTemplate.opsForValue().get(key);
        return val != null ? Long.parseLong(val) : 0L;
    }

    // 공간별 완료 횟수 조회
    public long getSpaceCount(Long userId, String space) {
        String key = String.format("%s:%d:%s", PREFIX_SPACE, userId, space);
        String val = redisTemplate.opsForValue().get(key);
        return val != null ? Long.parseLong(val) : 0L;
    }

    // 특정 집안일별 완료 횟수 조회
    public long getTitleCount(Long userId, String title) {
        String key = String.format("%s:%d:%s", PREFIX_TITLE, userId, title);
        String val = redisTemplate.opsForValue().get(key);
        return val != null ? Long.parseLong(val) : 0L;
    }


}
