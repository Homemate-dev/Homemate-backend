package com.zerobase.homemate.auth.support;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCookieFactory {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    //public static final String DOMAIN = ".homemate.io.kr";
    @Value("${auth.jwt.refresh-exp-seconds}")
    private long refreshExp;
    @Value("${auth.dev.enabled}")
    private boolean devEnabled;

    public ResponseCookie fromRefreshToken(String refreshToken) {
        return buildCookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken, refreshExp);
    }

    public ResponseCookie deleteRefreshToken() {
        return buildCookie(REFRESH_TOKEN_COOKIE_NAME, null, 0);
    }

    private ResponseCookie buildCookie(String key, String value, long expiration) {
        return ResponseCookie.from(key, value)
                .httpOnly(true)
                .sameSite("None")
                .secure(true)
                .maxAge(expiration)
                //.domain(DOMAIN)
                .path(resolvePath())
                .build();
    }

    private String resolvePath() {
        return devEnabled ? "/test" : "/api";
    }
}
