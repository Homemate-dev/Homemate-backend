package com.zerobase.homemate.badge.service;

import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.entity.enums.TimeSlot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserBadgeStatsService {

    private final StringRedisTemplate redisTemplate;

    // Key Format
    private static final String STATS_KEY_FORMAT = "user:stats:%d";
    private static final String SPACE_KEY_FORMAT = "user:stats:%d:space";
    private static final String TITLE_KEY_FORMAT = "user:stats:%d:title";
    private static final String TIME_FORMAT = "user:stats:%d:time";
    private static final String STREAK_FORMAT = "user:stats:%d:streak";
    private static final String ALARM_FORMAT = "user:stats:%d:alarm";
    private static final String ACCUMULATIVE_FORMAT = "user:stats:%d:accumulative";

    // Field Format
    private static final String FIELD_TOTAL_COMPLETED = "total_completed";
    private static final String FIELD_TOTAL_REGISTERED = "total_registered";
    private static final String FIELD_MISSION_COUNT = "mission_count";
    private static final String FIELD_LAST_UPDATED = "last_updated";

    private static final String FIELD_STREAK_COUNT = "count";
    private static final String FIELD_STREAK_LAST_DATE = "last_date";
    private static final String FIELD_ACCUMULATIVE_ALARM = "accumulative_after_alarm";

    // Increment Methods
    public void incrementTotalCompleted(Long userId){
        String key =  String.format(STATS_KEY_FORMAT, userId);
        setLastUpdated(userId, Instant.now().getEpochSecond());

        redisTemplate.opsForHash().increment(key, FIELD_TOTAL_COMPLETED, 1);
    }

    public void incrementTotalRegistered(Long userId){

        String key =  String.format(STATS_KEY_FORMAT, userId);
        setLastUpdated(userId, Instant.now().getEpochSecond());

        redisTemplate.opsForHash().increment(key, FIELD_TOTAL_REGISTERED, 1);
    }

    public void incrementMissionCount(Long userId){
        String key =  String.format(STATS_KEY_FORMAT, userId);
        setLastUpdated(userId, Instant.now().getEpochSecond());

        redisTemplate.opsForHash().increment(key, FIELD_MISSION_COUNT, 1);
    }

    public void incrementSpaceCount(Long userId, Space space){
        String key =  String.format(SPACE_KEY_FORMAT, userId);
        setLastUpdated(userId, Instant.now().getEpochSecond());

        redisTemplate.opsForHash().increment(key, space.name(), 1);
    }

    public void incrementTitleCount(Long userId, String title){
        String key = String.format(TITLE_KEY_FORMAT, userId);
        setLastUpdated(userId, Instant.now().getEpochSecond());

        redisTemplate.opsForHash().increment(key, title, 1);
    }

    public void incrementTimeCount(Long userId, TimeSlot slot) {
        String key = String.format(TIME_FORMAT, userId);
        setLastUpdated(userId, Instant.now().getEpochSecond());

        redisTemplate.opsForHash().increment(key, slot.name(), 1);
    }

    public void updateStreak(Long userId, LocalDate today) {
        String key = String.format(STREAK_FORMAT, userId);

        Object lastDateObj =
                redisTemplate.opsForHash().get(key, FIELD_STREAK_LAST_DATE);

        LocalDate lastDate =
                lastDateObj == null ? null : LocalDate.parse(lastDateObj.toString());

        int streak = (int) parseLongSafe(
                redisTemplate.opsForHash().get(key, FIELD_STREAK_COUNT)
        );

        if (lastDate == null) {
            streak = 1;
        } else if (lastDate.equals(today)) {
            return; // 이미 오늘 처리됨
        } else if (lastDate.plusDays(1).equals(today)) {
            streak += 1;
        } else {
            streak = 1;
        }

        redisTemplate.opsForHash().put(key, FIELD_STREAK_LAST_DATE, today.toString());
        redisTemplate.opsForHash().put(key, FIELD_STREAK_COUNT, String.valueOf(streak));

    }

    public long increaseChoreCountAfterAlarm(Long userId) {
        if (!hasChangedAlarm(userId)) {
            return 0;
        }

        String key = String.format(ACCUMULATIVE_FORMAT, userId);

        return redisTemplate.opsForHash()
                .increment(key, FIELD_ACCUMULATIVE_ALARM, 1);
    }


    public boolean markAlarmChangedIfAbsent(Long userId) {
        String key = String.format(ALARM_FORMAT, userId);

        if (redisTemplate.hasKey(key)) {
            return false;
        }

        redisTemplate.opsForValue().set(key, String.valueOf(true));
        return true;
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
        Object v = redisTemplate.opsForHash().get(String.format(SPACE_KEY_FORMAT, userId), space.name());
        return parseLongSafe(v);
    }

    public long getTitleCount(Long userId, String title){
        Object v = redisTemplate.opsForHash().get(String.format(TITLE_KEY_FORMAT, userId), title);
        return parseLongSafe(v);
    }

    public long getTimeCount(Long userId, TimeSlot targetTimeSlot) {
        Object v = redisTemplate.opsForHash()
                .get(String.format(TIME_FORMAT, userId), targetTimeSlot.name());

        return parseLongSafe(v);
    }



    public long getStreakCount(Long userId) {
        String key = String.format(STREAK_FORMAT, userId);

        Object lastDateObj =
                redisTemplate.opsForHash().get(key, FIELD_STREAK_LAST_DATE);

        if (lastDateObj == null) return 0;

        LocalDate lastDate = LocalDate.parse(lastDateObj.toString());
        long streak = parseLongSafe(
                redisTemplate.opsForHash().get(key, FIELD_STREAK_COUNT)
        );

        LocalDate today = LocalDate.now();

        if (lastDate.equals(today) || lastDate.plusDays(1).equals(today)) {
            return streak;
        }

        return 0;
    }

    public boolean hasChangedAlarm(Long userId) {
        String key = String.format(ALARM_FORMAT, userId);
        return redisTemplate.hasKey(key);
    }

    public long getAccumulativeAfterAlarm(Long userId) {
        Object v = redisTemplate.opsForHash().get(
                String.format(ACCUMULATIVE_FORMAT, userId),
                FIELD_ACCUMULATIVE_ALARM
        );
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
