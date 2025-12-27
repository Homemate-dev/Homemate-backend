package com.zerobase.homemate.auth.controller;

import com.zerobase.homemate.auth.dto.SocialLoginDto;
import com.zerobase.homemate.auth.dto.TokenResponseDto.AuthTokenCreatedDto;
import com.zerobase.homemate.auth.dto.TokenResponseDto.AuthTokenResponseDto;
import com.zerobase.homemate.auth.service.AuthService;
import com.zerobase.homemate.auth.service.KakaoLoginService;
import com.zerobase.homemate.auth.support.BearerTokenExtractor;
import com.zerobase.homemate.auth.support.RefreshTokenCookieFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static com.zerobase.homemate.auth.support.RefreshTokenCookieFactory.REFRESH_TOKEN_COOKIE_NAME;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final KakaoLoginService kakaoLoginService;
    private final RefreshTokenCookieFactory refreshTokenCookieFactory;

    @PostMapping("/login/kakao")
    public ResponseEntity<SocialLoginDto.LoginResponse> kakao(
            @Valid @RequestBody SocialLoginDto.KakaoLoginRequest request) {
        return ResponseEntity.ok(kakaoLoginService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthTokenResponseDto> refresh(
            @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME) String refreshToken) {

        AuthTokenCreatedDto result = authService.refresh(refreshToken);
        AuthTokenResponseDto body = new AuthTokenResponseDto(result.accessToken());

        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
        Optional<String> rt = result.refreshToken();
        if (rt.isPresent()) {
            ResponseCookie responseCookie = refreshTokenCookieFactory.fromRefreshToken(rt.get());
            builder = builder.header(HttpHeaders.SET_COOKIE, responseCookie.toString());
        }

        return builder.body(body);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        authService.logout(BearerTokenExtractor.resolveBearerToken(authorization));
        return ResponseEntity.noContent().build();
    }
}
