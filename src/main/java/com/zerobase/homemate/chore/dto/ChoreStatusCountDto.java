package com.zerobase.homemate.chore.dto;

public record ChoreStatusCountDto(
        Long choreId,
        long pendingCount,
        long completedCount
) {
}
