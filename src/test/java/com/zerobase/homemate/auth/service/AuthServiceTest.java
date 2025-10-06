package com.zerobase.homemate.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import com.zerobase.homemate.auth.dto.AuthTokenResponseDto;
import com.zerobase.homemate.auth.token.RefreshTokenStore;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
  private final JwtService jwtService = mock(JwtService.class);
  private final RefreshTokenStore refreshTokenStore = mock(RefreshTokenStore.class);
  private final UserRepository userRepository = mock(UserRepository.class);

  @Test
  @DisplayName("토큰 갱신 성공")
  void refresh_success() {
    // given
    var sut = new AuthService(jwtService, refreshTokenStore, userRepository);
    var rt = "rt";
    var userId = 2L;
    var sid = "sid-1";
    var oldJti = "jti-old";
    var newAT = "newAT";
    var newRT = "newRT";
    var newJti = "jti-new";

    Jws dummyJws = mock(Jws.class);
    given(jwtService.parse(rt)).willReturn(dummyJws);
    given(jwtService.getType(rt)).willReturn("RT");
    given(jwtService.getSubjectAsLong(rt)).willReturn(userId);
    given(jwtService.getSid(rt)).willReturn(sid);
    given(jwtService.getJti(rt)).willReturn(oldJti);
    given(refreshTokenStore.isCurrent(userId, sid, oldJti)).willReturn(true);

    var user = User.builder()
        .id(userId)
        .profileName("nick")
        .build();
    given(userRepository.findById(userId)).willReturn(Optional.of(user));

    given(jwtService.createAccessToken(user, sid)).willReturn(newAT);
    given(jwtService.createRefreshToken(userId, sid)).willReturn(newRT);
    given(jwtService.getJti(newRT)).willReturn(newJti);
    given(refreshTokenStore.rotate(userId, sid, oldJti, newJti)).willReturn(true);

    given(jwtService.getAccessTokenValiditySeconds()).willReturn(900L);
    given(jwtService.getRefreshTokenValiditySeconds()).willReturn(1_209_600L);

    // when
    var res = sut.refresh(rt);

    // then
    assertThat(res).isEqualTo(new AuthTokenResponseDto("Bearer", newAT, 900L, newRT, 1_209_600L));

    InOrder io = inOrder(jwtService, refreshTokenStore);
    io.verify(jwtService).parse(rt);
    io.verify(refreshTokenStore).isCurrent(userId, sid, oldJti);
    io.verify(jwtService).createAccessToken(user, sid);
    io.verify(jwtService).createRefreshToken(userId, sid);
    io.verify(refreshTokenStore).rotate(userId, sid, oldJti, newJti);
  }

  @Test
  @DisplayName("토큰 갱신 실패 - 재사용 탐지 시 즉시 삭제 후 예외")
  void refresh_reuse_detected() {
    // given
    var sut = new AuthService(jwtService, refreshTokenStore, userRepository);
    var rt = "rt";
    var userId = 2L;
    var sid = "sid-1";
    var jti = "jti-old";

    Jws dummyJws = mock(Jws.class);
    given(jwtService.parse(rt)).willReturn(dummyJws);
    given(jwtService.getType(rt)).willReturn("RT");
    given(jwtService.getSubjectAsLong(rt)).willReturn(userId);
    given(jwtService.getSid(rt)).willReturn(sid);
    given(jwtService.getJti(rt)).willReturn(jti);
    given(refreshTokenStore.isCurrent(userId, sid, jti)).willReturn(false);

    // when & then
    assertThatThrownBy(() -> sut.refresh(rt))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REFRESH_TOKEN_REUSED);

    then(refreshTokenStore).should().delete(userId, sid);
    then(userRepository).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("토큰 갱신 실패 - RT 만료")
  void refresh_expired_token() {
    var sut = new AuthService(jwtService, refreshTokenStore, userRepository);
    var rt = "expired";

    willThrow(new ExpiredJwtException(null, null, "expired")).given(jwtService).parse(rt);

    assertThatThrownBy(() -> sut.refresh(rt))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOKEN_EXPIRED);
  }

  @Test
  @DisplayName("토큰 갱신 실패 - 동시 갱신(원자 회전 실패)")
  void refresh_concurrent_failure() {
    var sut = new AuthService(jwtService, refreshTokenStore, userRepository);
    var rt = "rt";
    var userId = 2L;
    var sid = "sid-1";
    var jti = "jti-old";
    var newRT = "newRT";
    var newJti = "jti-new";
    var user = User.builder()
                .id(userId)
                .build();

    Jws dummyJws = mock(Jws.class);
    given(jwtService.parse(rt)).willReturn(dummyJws);
    given(jwtService.getType(rt)).willReturn("RT");
    given(jwtService.getSubjectAsLong(rt)).willReturn(userId);
    given(jwtService.getSid(rt)).willReturn(sid);
    given(jwtService.getJti(rt)).willReturn(jti);
    given(refreshTokenStore.isCurrent(userId, sid, jti)).willReturn(true);
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(jwtService.createAccessToken(user, sid)).willReturn("at");
    given(jwtService.createRefreshToken(userId, sid)).willReturn(newRT);
    given(jwtService.getJti(newRT)).willReturn(newJti);
    given(refreshTokenStore.rotate(userId, sid, jti, newJti)).willReturn(false);

    assertThatThrownBy(() -> sut.refresh(rt))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONCURRENT_REFRESH);
  }
}
