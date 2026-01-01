package com.zerobase.homemate.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.zerobase.homemate.auth.dto.SocialLoginDto;
import com.zerobase.homemate.auth.kakao.KakaoClient;
import com.zerobase.homemate.auth.kakao.KakaoDto;
import com.zerobase.homemate.entity.enums.SocialProvider;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KakaoLoginServiceTest {
  private final KakaoClient kakaoClient = mock(KakaoClient.class);
  private final KakaoLoginTransaction kakaoLoginTx = mock(KakaoLoginTransaction.class);

  private final KakaoLoginService sut = new KakaoLoginService(kakaoClient, kakaoLoginTx);

  @Test
  @DisplayName("로그인 성공")
  void login_success() {
    // given
    SocialLoginDto.KakaoLoginRequest request =
        new SocialLoginDto.KakaoLoginRequest(
            "authCode-123", "http://localhost/callback", "verifier-xyz");

    KakaoDto.TokenResponse tokenRes = mock(KakaoDto.TokenResponse.class);
    given(tokenRes.accessToken()).willReturn("kakaoAT");

    KakaoDto.ProfileResponse profile = mock(KakaoDto.ProfileResponse.class);

    given(kakaoClient.exchangeToken(request.authorizationCode(), request.redirectUri(), request.codeVerifier()))
        .willReturn(tokenRes);
    given(kakaoClient.fetchProfile("kakaoAT")).willReturn(profile);

    SocialLoginDto.InternalLoginResponse expected = new SocialLoginDto.InternalLoginResponse(
        new SocialLoginDto.LoginResponse("ourAT",
        new SocialLoginDto.UserDto(
            1L, SocialProvider.KAKAO, "12345", "Nick", "https://img", false
        )),
        "ourRt"
    );
    given(kakaoLoginTx.upsertAndIssue(profile)).willReturn(expected);

    // when
    SocialLoginDto.InternalLoginResponse actual = sut.login(request);

    // then
    assertThat(actual).isNotNull();
    assertThat(actual.loginResponse().accessToken()).isEqualTo("ourAT");

    then(kakaoClient).should().exchangeToken(request.authorizationCode(), request.redirectUri(), request.codeVerifier());
    then(kakaoClient).should().fetchProfile("kakaoAT");
    then(kakaoLoginTx).should().upsertAndIssue(profile);
  }

  @Test
  @DisplayName("카카오 토큰 교환 실패 -> 도메인 예외")
  void kakao_login_error() {
    // given
    SocialLoginDto.KakaoLoginRequest request =
        new SocialLoginDto.KakaoLoginRequest("bad-code", "http://localhost/callback", "verifier");

    given(kakaoClient.exchangeToken(any(), any(), any()))
        .willThrow(new CustomException(ErrorCode.INVALID_AUTH_CODE));

    // when / then
    assertThatThrownBy(() -> sut.login(request))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(ErrorCode.INVALID_AUTH_CODE.getMessage());

    then(kakaoClient).should().exchangeToken(any(), any(), any());
    then(kakaoClient).shouldHaveNoMoreInteractions();
    then(kakaoLoginTx).shouldHaveNoInteractions();
  }
}
