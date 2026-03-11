package com.zerobase.homemate.auth.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KakaoDto {
  // 토큰 응답
  public record TokenResponse(
      @JsonProperty("access_token") String accessToken,
      @JsonProperty("token_type") String tokenType,
      @JsonProperty("expires_in") Long expiresIn,
      @JsonProperty("refresh_token") String refreshToken,
      @JsonProperty("refresh_token_expires_in") Long refreshTokenExpiresIn
  ) {}

  // 프로필 응답
  public record ProfileResponse(
      Long id,
      Properties properties
  ) {
    public record Properties(
      String nickname,
      @JsonProperty("profile_image") String profileImage
    ) {}
  }
}
