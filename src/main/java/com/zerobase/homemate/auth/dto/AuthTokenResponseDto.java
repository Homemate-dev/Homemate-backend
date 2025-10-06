package com.zerobase.homemate.auth.dto;

public record AuthTokenResponseDto(
    String tokenType,
    String accessToken,
    long accessTokenExpiresIn,
    String refreshToken,
    long refreshTokenExpiresIn
) {}
