package com.zerobase.homemate.auth.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;

import static com.zerobase.homemate.auth.support.RefreshTokenCookieFactory.REFRESH_TOKEN_COOKIE_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RefreshTokenCookieFactoryTest {

    @InjectMocks
    private RefreshTokenCookieFactory refreshTokenCookieFactory;

    @Test
    @DisplayName("리프레시 토큰 쿠키 생성 테스트")
    void createRefreshToken() {
        String refreshToken = "RefreshToken";

        ResponseCookie result = refreshTokenCookieFactory.fromRefreshToken(refreshToken);

        assertThat(result.getName()).isEqualTo(REFRESH_TOKEN_COOKIE_NAME);
        assertThat(result.getValue()).isEqualTo(refreshToken);
        assertThat(result.isHttpOnly()).isTrue();
        assertThat(result.getSameSite()).isEqualTo("Lax");
        assertThat(result.isSecure()).isTrue();
    }
}