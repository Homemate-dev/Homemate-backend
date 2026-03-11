package com.zerobase.homemate.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobase.homemate.auth.dto.SocialLoginDto;
import com.zerobase.homemate.auth.service.AuthService;
import com.zerobase.homemate.auth.service.JwtService;
import com.zerobase.homemate.auth.service.KakaoLoginService;
import com.zerobase.homemate.auth.support.RefreshTokenCookieFactory;
import com.zerobase.homemate.entity.enums.SocialProvider;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    @MockitoBean
    RefreshTokenCookieFactory refreshTokenCookieFactory;

    @Test
    @DisplayName("카카오 로그인 성공")
    void kakao_login_success() throws Exception {
        // given
        var req = new SocialLoginDto.KakaoLoginRequest(
                "code", "http://localhost/callback", "verifier");

        SocialLoginDto.InternalLoginResponse response = new SocialLoginDto.InternalLoginResponse(
                new SocialLoginDto.LoginResponse(
                        "ourAT",
                        new SocialLoginDto.UserDto(
                                1L, SocialProvider.KAKAO, "12345", "Nick", "https://img", true
                        )
                ),
                "ourRT");
        given(kakaoLoginService.login(any())).willReturn(response);
        given(refreshTokenCookieFactory.fromRefreshToken("ourRT")).willReturn(ResponseCookie.from("ourRT").build());

        // when & then
        mockMvc.perform(post("/auth/login/kakao")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(jsonPath("$.accessToken").value("ourAT"))
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
}
