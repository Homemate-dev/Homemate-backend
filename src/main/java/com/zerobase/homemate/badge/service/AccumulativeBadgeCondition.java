package com.zerobase.homemate.badge.service;

import com.zerobase.homemate.entity.Chore;

public class AccumulativeBadgeCondition implements BadgeCondition {
    @Override
    public boolean matchesCondition(Chore chore) {
        return false;
    }
}
