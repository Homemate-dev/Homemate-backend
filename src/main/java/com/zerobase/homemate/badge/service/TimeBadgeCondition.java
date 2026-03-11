package com.zerobase.homemate.badge.service;

import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.enums.TimeSlot;

public class TimeBadgeCondition implements BadgeCondition {

    private final TimeSlot targetTimeSlot;
    private final int required;
    private final UserBadgeStatsService userBadgeStatsService;

    public TimeBadgeCondition(TimeSlot targetTimeSlot, int required, UserBadgeStatsService userBadgeStatsService) {
        this.targetTimeSlot = targetTimeSlot;
        this.required = required;
        this.userBadgeStatsService = userBadgeStatsService;
    }

    @Override
    public boolean matchesCondition(Chore chore) {
        return userBadgeStatsService.getTimeCount(chore.getUser().getId(), targetTimeSlot) >= required;

    }
}
