package com.zerobase.homemate.badge.service;

import com.zerobase.homemate.entity.Chore;

public class TotalBadgeCondition implements BadgeCondition {

    private final int requiredCount;
    private final UserBadgeStatsService userBadgeStatsService;

    public TotalBadgeCondition(int requiredCount, UserBadgeStatsService userBadgeStatsService) {
        this.requiredCount = requiredCount;
        this.userBadgeStatsService = userBadgeStatsService;
    }

    @Override
    public boolean matchesCondition(Chore chore) {
        Long userId = chore.getUser().getId();
        long completedCount = userBadgeStatsService.getTotalCompletedCount(userId);
        return completedCount >= requiredCount;
    }
}
