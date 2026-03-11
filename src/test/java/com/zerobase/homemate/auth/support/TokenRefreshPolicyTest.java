package com.zerobase.homemate.auth.support;


import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class TokenRefreshPolicyTest {

    @InjectMocks
    private TokenRefreshPolicy tokenRefreshPolicy;

    @Test
    @DisplayName("고정길이 정책 - 갱신 필요한 토큰 -> true 반환")
    void returnsTrue_WhenRemainingDaysLessThanPolicy_WithFixedPolicy() {
        // given
        Claims claims = mock(Claims.class);
        given(claims.getExpiration()).willReturn(Date.from(Instant.now().minus(TokenRefreshPolicy.FIXED_POLICY_DAYS - 1, ChronoUnit.DAYS)));

        // when
        boolean result = tokenRefreshPolicy.shouldRotateRefreshTokenByFixedDuration(claims);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("고정길이 정책 - 갱신 불필요한 토큰 -> false 반환")
    void returnsFalse_WhenRemainingDaysGreaterThanPolicy_WithFixedPolicy() {
        // given
        Claims claims = mock(Claims.class);
        given(claims.getExpiration()).willReturn(Date.from(Instant.now().minus(TokenRefreshPolicy.FIXED_POLICY_DAYS + 1, ChronoUnit.DAYS)));

        // when
        boolean result = tokenRefreshPolicy.shouldRotateRefreshTokenByFixedDuration(claims);

        // then
        assertThat(result).isFalse();
    }
}