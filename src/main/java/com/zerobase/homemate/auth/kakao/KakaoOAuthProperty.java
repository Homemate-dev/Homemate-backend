package com.zerobase.homemate.auth.kakao;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.kakao")
public record KakaoOAuthProperty(
    String clientId,
    String clientSecret,
    String tokenUri,
    String profileUri
) {}
