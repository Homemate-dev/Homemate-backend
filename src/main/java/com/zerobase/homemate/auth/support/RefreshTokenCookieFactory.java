package com.zerobase.homemate.auth.support;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCookieFactory {

    @Value("${auth.jwt.refresh-exp-seconds}")
    private long refreshExp;

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    public ResponseCookie fromRefreshToken(String refreshToken) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .sameSite("Lax")
                .secure(true)
                .maxAge(refreshExp)
                .build();
    }

    public ResponseCookie deleteRefreshToken() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .sameSite("Lax")
                .secure(true)
                .maxAge(0)
                .build();
    }
}
