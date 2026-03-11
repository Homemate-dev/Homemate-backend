package com.zerobase.homemate.badge.service;

import com.zerobase.homemate.entity.Chore;

public class AlarmBadgeCondition implements BadgeCondition {
    @Override
    public boolean matchesCondition(Chore chore) {
        return false;
    }
}
