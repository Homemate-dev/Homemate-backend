package com.zerobase.homemate.auth.dto;

import com.zerobase.homemate.entity.enums.SocialProvider;
import jakarta.validation.constraints.NotBlank;

public class SocialLoginDto {
  public record KakaoLoginRequest(
      @NotBlank String authorizationCode,
      @NotBlank String redirectUri,
      @NotBlank String codeVerifier
  ) {}

  public record LoginResponse(
      String tokenType,
      String accessToken,
      long accessTokenExpiresIn,
      String refreshToken,
      long refreshTokenExpiresIn,
      UserDto user
  ) {
    public record UserDto(
        Long id,
        SocialProvider provider,
        String providerUserId,
        String nickname,
        String profileImageUrl,
        boolean isNewUser
    ) {}
  }
}
