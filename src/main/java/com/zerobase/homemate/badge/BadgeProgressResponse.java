package com.zerobase.homemate.badge;

import com.zerobase.homemate.entity.enums.BadgeType;

import java.time.LocalDateTime;

public record BadgeProgressResponse (
        BadgeType badgeType,
        String badgeTitle,
        String description,
        boolean acquired,
        LocalDateTime acquiredAt,
        int currentCount,
        int requiredCount,
        int remainingCount,
        String badgeImageUrl
){
    public static BadgeProgressResponse of(
            BadgeType type,
            int currentCount,
            boolean acquired,
            LocalDateTime acquiredAt
    ) {
        int targetCount = type.getRequireCount();
        int remainingCount = Math.max(0, targetCount - currentCount);

        return new BadgeProgressResponse(
                type,
                type.getBadgeName(),
                type.getDescription(),
                acquired,
                acquiredAt,
                currentCount,
                type.getRequireCount(),
                remainingCount,
                type.getBadgeImageUrl()
        );
    }

}
