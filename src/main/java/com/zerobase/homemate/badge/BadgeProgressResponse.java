package com.zerobase.homemate.badge;

import com.zerobase.homemate.entity.enums.BadgeType;

public record BadgeProgressResponse (
        BadgeType badgeType,
        String badgeTitle,
        String description,
        boolean acquired,
        int currentCount,
        int requiredCount,
        int remainingCount,
        String badgeImageUrl
){
    public static BadgeProgressResponse of(
            BadgeType type,
            int currentCount
    ) {
        int targetCount = type.getRequireCount();
        int remainingCount = Math.max(0, targetCount - currentCount);
        boolean acquired = currentCount >= targetCount;

        return new BadgeProgressResponse(
                type,
                type.getBadgeName(),
                type.getDescription(),
                acquired,
                currentCount,
                type.getRequireCount(),
                remainingCount,
                type.getBadgeImageUrl()
        );
    }

}
