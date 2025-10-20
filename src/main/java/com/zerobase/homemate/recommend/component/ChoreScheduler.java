package com.zerobase.homemate.recommend.component;

import com.zerobase.homemate.recommend.service.stats.DbChoreStatsService;
import com.zerobase.homemate.recommend.service.stats.RedisChoreStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChoreScheduler {

    private final RedisChoreStatsService redisChoreStatsService;
    private final DbChoreStatsService dbChoreStatsService;

    // 매달 1일 0시 0분 0초 실행
    @Scheduled(cron = "0 0 0 1 * *")
    public void resetMonthlyStats(){
        log.info("Monthly Redis Chore stats reset Starting...");
        redisChoreStatsService.resetAllCounts();
        log.info("Monthly Redis Chore stats reset Completed.");
    }

    // 격주 월요일 0시 0분 0초에 실행
    @Scheduled(cron = "0 0 0 ? * MON#1,MON#3")
    public void syncBiWeekly(){
        log.info("Bi-Weekly Redis -> DB sync Starting...");
        dbChoreStatsService.syncFromRedis(
                redisChoreStatsService.getCategoryStats(),
                redisChoreStatsService.getSpaceStats()
        );

        log.info("Bi-Weekly Redis sync Completed.");
    }


}
