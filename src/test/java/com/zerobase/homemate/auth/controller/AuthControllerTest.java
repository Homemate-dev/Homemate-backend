package com.zerobase.homemate.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobase.homemate.auth.dto.AuthTokenResponseDto;
import com.zerobase.homemate.auth.dto.SocialLoginDto;
import com.zerobase.homemate.auth.service.AuthService;
import com.zerobase.homemate.auth.service.JwtService;
import com.zerobase.homemate.auth.service.KakaoLoginService;
import com.zerobase.homemate.entity.enums.SocialProvider;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
  @Autowired
  MockMvc mockMvc;
  @Autowired
  ObjectMapper om;

  @MockitoBean
  KakaoLoginService kakaoLoginService;
  @MockitoBean
  JwtService jwtService;
  @MockitoBean
  AuthService authService;
  @MockitoBean
  UserRepository userRepository;

  @Test
  @DisplayName("카카오 로그인 성공")
  void kakao_login_success() throws Exception {
    // given
    var req = new SocialLoginDto.KakaoLoginRequest(
        "code", "http://localhost/callback", "verifier");

    var resp = new SocialLoginDto.LoginResponse(
        "Bearer", "ourAT", 900L, "ourRT", 1_209_600L,
        new SocialLoginDto.LoginResponse.UserDto(
            1L, SocialProvider.KAKAO, "12345", "Nick", "https://img", true
        )
    );
    given(kakaoLoginService.login(any())).willReturn(resp);

    // when & then
    mockMvc.perform(post("/auth/login/kakao")
            .contentType(String.valueOf(MediaType.APPLICATION_JSON))
            .content(om.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(String.valueOf(MediaType.APPLICATION_JSON)))
        .andExpect(jsonPath("$.tokenType").value("Bearer"))
        .andExpect(jsonPath("$.accessToken").value("ourAT"))
        .andExpect(jsonPath("$.accessTokenExpiresIn").value(900))
        .andExpect(jsonPath("$.refreshToken").value("ourRT"))
        .andExpect(jsonPath("$.refreshTokenExpiresIn").value(1209600))
        .andExpect(jsonPath("$.user.id").value(1))
        .andExpect(jsonPath("$.user.provider").value("KAKAO"))
        .andExpect(jsonPath("$.user.providerUserId").value("12345"))
        .andExpect(jsonPath("$.user.nickname").value("Nick"))
        .andExpect(jsonPath("$.user.profileImageUrl").value("https://img"))
        .andExpect(jsonPath("$.user.isNewUser").value(true));
  }

  @Test
  @DisplayName("카카오 토큰 교환 실패 -> 도메인 예외")
  void kakao_login_error() throws Exception {
    given(kakaoLoginService.login(any()))
        .willThrow(new CustomException(ErrorCode.INVALID_AUTH_CODE));

    mockMvc.perform(post("/auth/login/kakao")
            .contentType(String.valueOf(MediaType.APPLICATION_JSON))
            .content(om.writeValueAsString(new SocialLoginDto.KakaoLoginRequest(
                "bad", "http://localhost/cb", "v"))))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.httpStatus").value(401))
        .andExpect(jsonPath("$.error.code").value("INVALID_AUTH_CODE"));
   }

  @Test
  @DisplayName("RT로 갱신 성공")
  void refresh_success() throws Exception {
    // given
    var rt = "refresh.jwt.token";

    var resp = new AuthTokenResponseDto(
        "Bearer", "newAT", 900L, "newRT", 1_209_600L
    );
    given(authService.refresh(eq(rt))).willReturn(resp);

    // when & then
    mockMvc.perform(post("/auth/refresh")
            // 규약: Bearer 없음, RT 그대로 전송
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + rt))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.tokenType").value("Bearer"))
        .andExpect(jsonPath("$.accessToken").value("newAT"))
        .andExpect(jsonPath("$.accessTokenExpiresIn").value(900))
        .andExpect(jsonPath("$.refreshToken").value("newRT"))
        .andExpect(jsonPath("$.refreshTokenExpiresIn").value(1_209_600));
  }

  @Test
  @DisplayName("로그아웃 성공")
  void logout_success() throws Exception {
    var at = "Bearer access.jwt.token";

    willDoNothing().given(authService).logout(eq(at));

    mockMvc.perform(post("/auth/logout")
            .header("Authorization", at))
        .andExpect(status().isNoContent());
  }
}
