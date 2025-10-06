package com.zerobase.homemate.auth.service;

import com.zerobase.homemate.auth.dto.AuthTokenResponseDto;
import com.zerobase.homemate.auth.token.RefreshTokenStore;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final JwtService jwtService;
  private final RefreshTokenStore refreshTokenStore;
  private final UserRepository userRepository;

  public AuthTokenResponseDto refresh(String refreshToken) {
    // 토큰 유효성 검증(서명/만료)
    try {
      jwtService.parse(refreshToken);
    } catch (ExpiredJwtException e) {
      throw new CustomException(ErrorCode.TOKEN_EXPIRED);
    } catch (JwtException e) {
      throw new CustomException(ErrorCode.INVALID_TOKEN);
    }

    // 토큰 타입 검증
    if(!"RT".equals(jwtService.getType(refreshToken))) {
      throw new CustomException(ErrorCode.INVALID_TOKEN_TYPE_REFRESH);
    }

    long userId = jwtService.getSubjectAsLong(refreshToken);
    String sid = jwtService.getSid(refreshToken);
    String jti = jwtService.getJti(refreshToken);

    // 재사용 탐지 : 현재 저장된 jti와 일치해야 함
    if (!refreshTokenStore.isCurrent(userId, sid, jti)) {
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
}
