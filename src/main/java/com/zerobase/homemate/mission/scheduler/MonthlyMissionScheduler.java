package com.zerobase.homemate.mission.scheduler;

import com.zerobase.homemate.mission.service.MissionAssignmentService;

import java.time.YearMonth;
import java.time.ZoneId;

import com.zerobase.homemate.recommend.service.MonthlyCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MonthlyMissionScheduler {

    private final MissionAssignmentService missionAssignmentService;
    private final MonthlyCategoryService monthlyCategoryService;

    // 매월 1일 00:01 KST 배치
    @Scheduled(cron = "0 1 0 1 * *", zone = "Asia/Seoul")
    public void assignMonthlyMissions() {
        YearMonth yearMonth = YearMonth.now(ZoneId.of("Asia/Seoul"));
        missionAssignmentService.assignForMonth(yearMonth);
        monthlyCategoryService.updateMonthlyMissionChores();
    }

}
