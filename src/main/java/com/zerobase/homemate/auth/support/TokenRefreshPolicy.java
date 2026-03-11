package com.zerobase.homemate.auth.support;

import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class TokenRefreshPolicy {

    public static final int FIXED_POLICY_DAYS = 3;

    public boolean shouldRotateRefreshTokenByFixedDuration(Claims claims) {
        Instant now = Instant.now();
        Instant exp = claims.getExpiration().toInstant();
        long daysBetween = Duration.between(exp, now).toDays();

        return daysBetween <= FIXED_POLICY_DAYS;
    }
}
