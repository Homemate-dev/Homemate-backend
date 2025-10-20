package com.zerobase.homemate.badge;

import com.zerobase.homemate.entity.enums.BadgeType;

public record BadgeResponse (
        BadgeType type,
        boolean acquired,
        int remainingCount
){
}
