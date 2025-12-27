package com.zerobase.homemate.auth.dto;

import java.util.Optional;

public class TokenResponseDto {
    // 액세스 토큰 반환용 DTO
    public record AuthTokenResponseDto(
            String accessToken
    ) {}

    // 내부 사용 용도의 DTO
    public record AuthTokenCreatedDto(
            String accessToken,
            Optional<String> refreshToken
    ) {}
}


