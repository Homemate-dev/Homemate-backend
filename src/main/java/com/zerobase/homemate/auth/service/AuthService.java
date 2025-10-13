package com.zerobase.homemate.auth.service;

import com.zerobase.homemate.auth.dto.AuthTokenResponseDto;
import com.zerobase.homemate.auth.token.AccessTokenBlocklist;
import com.zerobase.homemate.auth.token.RefreshTokenStore;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.repository.UserRepository;
import io.jsonwebtoken.Claims;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final JwtService jwtService;
  private final AccessTokenBlocklist accessTokenBlocklist;
  private final RefreshTokenStore refreshTokenStore;
  private final UserRepository userRepository;

  public AuthTokenResponseDto refresh(String refreshToken) {
    Claims claims = jwtService.parseAndValidateType(refreshToken, "RT");

    long userId = Long.parseLong(claims.getSubject());
    String sid = claims.get("sid", String.class);
    String jti = claims.getId();

    // 재사용 탐지 : 현재 저장된 jti와 일치해야 함
    if (!refreshTokenStore.matchesCurrentJti(userId, sid, jti)) {
      refreshTokenStore.delete(userId, sid);  // 해당 sid의 현재 RT 상태를 즉시 무효화
      throw new CustomException(ErrorCode.REFRESH_TOKEN_REUSED);
    }

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    // 새 토큰 발급
    String newAccessToken = jwtService.createAccessToken(user, sid);
    String newRefreshToken = jwtService.createRefreshToken(userId, sid);
    String newJti = jwtService.getJti(newRefreshToken);

    // 동시 갱신 경합 방지
    boolean rotated = refreshTokenStore.rotate(userId, sid, jti, newJti);
    if (!rotated) {
      throw new CustomException(ErrorCode.CONCURRENT_REFRESH);
    }

    return new AuthTokenResponseDto(
        "Bearer",
        newAccessToken,
        jwtService.getAccessTokenValiditySeconds(),
        newRefreshToken,
        jwtService.getRefreshTokenValiditySeconds());
  }

  public void logout(String accessToken) {
    Claims claims = jwtService.parseAndValidateType(accessToken, "AT");

    long userId = Long.parseLong(claims.getSubject());
    String sid = claims.get("sid", String.class);
    String jti = claims.getId();

    Instant exp = claims.getExpiration().toInstant();
    Duration ttl = Duration.between(Instant.now(), exp);

    // AT 블록 / RT 삭제
    accessTokenBlocklist.block(jti, ttl);
    refreshTokenStore.delete(userId, sid);
  }
}
