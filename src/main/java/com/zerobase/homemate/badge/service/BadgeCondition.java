package com.zerobase.homemate.badge.service;

import com.zerobase.homemate.entity.Chore;

public interface BadgeCondition {
    boolean matchesCondition(Chore chore);
}
