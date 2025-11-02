package com.zerobase.homemate.badge.service;

import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.User;
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
    public boolean matchesCondition(User user, Chore chore) {
        // 예상 및 실제 space
        Space choreSpace = chore.getSpace();
        boolean sameSpace = (choreSpace == targetSpace);

        long completedCount = userBadgeStatsService.getSpaceCount(chore.getUser().getId(), targetSpace);
        boolean meetsCount = completedCount >= requiredCount;

        return sameSpace && meetsCount;
    }


    @Override
    public String getBadgeName() {
        return badgeName;
    }
}
