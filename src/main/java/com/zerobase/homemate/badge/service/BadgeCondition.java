package com.zerobase.homemate.badge.service;

import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.User;

public interface BadgeCondition {
    boolean matchesCondition(User user, Chore chore);
}
