package com.zerobase.homemate.badge.service;

import com.zerobase.homemate.entity.Chore;

public class StreakBadgeCondition implements BadgeCondition {

    private final int requiredCount;
    private final UserBadgeStatsService userBadgeStatsService;

    public StreakBadgeCondition(int requiredCount, UserBadgeStatsService userBadgeStatsService) {
        this.requiredCount = requiredCount;
        this.userBadgeStatsService = userBadgeStatsService;
    }

    @Override
    public boolean matchesCondition(Chore chore) {
        return userBadgeStatsService.getStreakCount(chore.getUser().getId()) >= requiredCount;
    }
}
