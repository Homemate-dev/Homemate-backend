package com.zerobase.homemate.auth.controller;

import com.zerobase.homemate.auth.dto.AuthTokenResponseDto;
import com.zerobase.homemate.auth.dto.SocialLoginDto;
import com.zerobase.homemate.auth.dto.SocialLoginDto.LoginResponse;
import com.zerobase.homemate.auth.service.AuthService;
import com.zerobase.homemate.auth.service.KakaoLoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    LoginResponse response = kakaoLoginService.login(request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthTokenResponseDto> refresh(@RequestHeader("Authorization") String refreshToken) {
    return ResponseEntity.ok(authService.refresh(refreshToken));
  }
}
