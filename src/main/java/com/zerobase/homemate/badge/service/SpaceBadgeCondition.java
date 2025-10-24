package com.zerobase.homemate.badge.service;

import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.enums.Space;

public class SpaceBadgeCondition implements BadgeCondition {

    private final Space targetSpace;
    private final int requiredCount;
    private final String badgeName;
    private final UserBadgeStatsService userBadgeStatsService;

    public SpaceBadgeCondition(Space targetSpace, int requiredCount, String badgeName, UserBadgeStatsService userBadgeStatsService) {
        this.targetSpace = targetSpace;
        this.requiredCount = requiredCount;
        this.badgeName = badgeName;
        this.userBadgeStatsService = userBadgeStatsService;
    }

    @Override
    public boolean matchesCondition(Chore chore) {

        if(chore.getSpace() != targetSpace) {
            return false;
        }

        long completedCount = userBadgeStatsService.getSpaceCount(chore.getUser().getId(), targetSpace.name());

        return completedCount >= requiredCount;
    }

    @Override
    public String getBadgeName() {
        return badgeName;
    }
}
