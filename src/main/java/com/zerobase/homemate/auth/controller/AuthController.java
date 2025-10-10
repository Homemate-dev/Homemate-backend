package com.zerobase.homemate.auth.controller;

import com.zerobase.homemate.auth.dto.AuthTokenResponseDto;
import com.zerobase.homemate.auth.dto.SocialLoginDto;
import com.zerobase.homemate.auth.service.AuthService;
import com.zerobase.homemate.auth.service.KakaoLoginService;
import com.zerobase.homemate.auth.support.BearerTokenExtractor;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
  private final AuthService authService;
  private final KakaoLoginService kakaoLoginService;

  @PostMapping("/login/kakao")
  public ResponseEntity<SocialLoginDto.LoginResponse> kakao(
      @Valid @RequestBody SocialLoginDto.KakaoLoginRequest request) {
    return ResponseEntity.ok(kakaoLoginService.login(request));
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthTokenResponseDto> refresh(@RequestHeader("Authorization") String refreshToken) {
    return ResponseEntity.ok(authService.refresh(refreshToken));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
    authService.logout(BearerTokenExtractor.resolveBearerToken(authorization));
    return ResponseEntity.noContent().build();
  }
}
