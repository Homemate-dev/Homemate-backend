package com.zerobase.homemate.badge.service;

import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.User;

public class MissionBadgeCondition implements BadgeCondition{

    private final int requiredCount;
    private final UserBadgeStatsService userBadgeStatsService;

    public MissionBadgeCondition(int requiredCount, UserBadgeStatsService userBadgeStatsService) {
        this.requiredCount = requiredCount;
        this.userBadgeStatsService = userBadgeStatsService;
    }

    @Override
    public boolean matchesCondition(Chore chore) {
        Long userId = chore.getUser().getId();
        long completedCount = userBadgeStatsService.getTotalMissionCount(userId);
        return completedCount >= requiredCount;
    }
}
