package com.zerobase.homemate.auth.controller;

import com.zerobase.homemate.auth.dto.SocialLoginDto;
import com.zerobase.homemate.auth.dto.TokenResponseDto.AuthTokenCreatedDto;
import com.zerobase.homemate.auth.dto.TokenResponseDto.AuthTokenResponseDto;
import com.zerobase.homemate.auth.dto.WithdrawRequestDto;
import com.zerobase.homemate.auth.security.UserPrincipal;
import com.zerobase.homemate.auth.service.AuthService;
import com.zerobase.homemate.auth.service.KakaoLoginService;
import com.zerobase.homemate.auth.support.BearerTokenExtractor;
import com.zerobase.homemate.auth.support.RefreshTokenCookieFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static com.zerobase.homemate.auth.support.RefreshTokenCookieFactory.REFRESH_TOKEN_COOKIE_NAME;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final KakaoLoginService kakaoLoginService;
    private final RefreshTokenCookieFactory refreshTokenCookieFactory;

    @PostMapping("/login/kakao")
    public ResponseEntity<SocialLoginDto.LoginResponse> kakao(
            @Valid @RequestBody SocialLoginDto.KakaoLoginRequest request
    ) {
        SocialLoginDto.InternalLoginResponse loginResult = kakaoLoginService.login(request);
        ResponseCookie responseCookie = refreshTokenCookieFactory.fromRefreshToken(loginResult.refreshToken());

        return createResponseWithCookie(loginResult.loginResponse(), OK, responseCookie);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthTokenResponseDto> refresh(
            @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME) String refreshToken
    ) {
        AuthTokenCreatedDto result = authService.refresh(refreshToken);
        AuthTokenResponseDto body = new AuthTokenResponseDto(result.accessToken());

        Optional<String> rt = result.newRefreshToken();
        if (rt.isPresent()) {
            ResponseCookie responseCookie = refreshTokenCookieFactory.fromRefreshToken(rt.get());
            return createResponseWithCookie(body, OK, responseCookie);
        }

        return ResponseEntity.ok(body);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        authService.logout(BearerTokenExtractor.resolveBearerToken(authorization));
        ResponseCookie deleteCookie = refreshTokenCookieFactory.deleteRefreshToken();

        return createResponseWithCookie(null, NO_CONTENT, deleteCookie);
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<Void> withdraw(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody WithdrawRequestDto requestDto
    ) {
        Long userId = userPrincipal.id();
        authService.withdraw(userId, requestDto);
        ResponseCookie deleteCookie = refreshTokenCookieFactory.deleteRefreshToken();

        return createResponseWithCookie(null, NO_CONTENT, deleteCookie);
    }

    private <T> ResponseEntity<T> createResponseWithCookie(T body, HttpStatus code, ResponseCookie cookie) {
        return ResponseEntity.status(code)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(body);
    }
}