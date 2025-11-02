package com.zerobase.homemate.badge.service;

import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.User;

public class MissionBadgeCondition implements BadgeCondition{

    private final int requiredCount;
    private final String badgeName;
    private final UserBadgeStatsService userBadgeStatsService;

    public MissionBadgeCondition(int requiredCount, String badgeName, UserBadgeStatsService userBadgeStatsService) {
        this.requiredCount = requiredCount;
        this.badgeName = badgeName;
        this.userBadgeStatsService = userBadgeStatsService;
    }

    @Override
    public boolean matchesCondition(User user, Chore chore) {
        Long userId = chore.getUser().getId();
        long completedCount = userBadgeStatsService.getMissionCount(userId);
        return completedCount >= requiredCount;
    }

    @Override
    public String getBadgeName() {
        return badgeName;
    }
}
