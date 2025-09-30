package com.zerobase.homemate.auth.service;

import com.zerobase.homemate.auth.dto.SocialLoginDto;
import com.zerobase.homemate.auth.kakao.KakaoClient;
import com.zerobase.homemate.auth.kakao.KakaoDto.ProfileResponse;
import com.zerobase.homemate.auth.kakao.KakaoDto.TokenResponse;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KakaoLoginService {
  private final KakaoClient kakaoClient;
  private final KakaoLoginTransaction kakaoLoginTransaction;

  public SocialLoginDto.LoginResponse login(SocialLoginDto.KakaoLoginRequest request) {
    if (request.codeVerifier() == null || request.codeVerifier().isBlank()) {
      throw new CustomException(ErrorCode.PKCE_VERIFIER_REQUIRED);
    }

    // 인가코드로 카카오 액세스 토큰 교환
    TokenResponse tokenRes = kakaoClient.exchangeToken(
        request.authorizationCode(), request.redirectUri(), request.codeVerifier());

    // 액세스 토큰으로 사용자 프로필 조회
    ProfileResponse profile = kakaoClient.fetchProfile(tokenRes.accessToken());

    return kakaoLoginTransaction.upsertAndIssue(profile);
  }
}
