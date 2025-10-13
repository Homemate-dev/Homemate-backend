package com.zerobase.homemate.mission.scheduler;

import com.zerobase.homemate.mission.service.MissionAssignmentService;
import java.time.YearMonth;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MonthlyMissionScheduler {

    private final MissionAssignmentService missionAssignmentService;

    // 매월 1일 00:01 KST 배치
    @Scheduled(cron = "0 1 0 1 * *", zone = "Asia/Seoul")
    @SchedulerLock(name = "assignMonthlyMissions", lockAtLeastFor = "1m",
        lockAtMostFor = "10m")
    public void assignMonthlyMissions() {
        YearMonth yearMonth = YearMonth.now(ZoneId.of("Asia/Seoul"));
        missionAssignmentService.assignForMonth(yearMonth);
    }

}
