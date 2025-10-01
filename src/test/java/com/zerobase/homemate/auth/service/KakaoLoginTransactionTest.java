package com.zerobase.homemate.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import com.zerobase.homemate.auth.dto.SocialLoginDto;
import com.zerobase.homemate.auth.kakao.KakaoDto;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.UserSocialAccount;
import com.zerobase.homemate.entity.enums.SocialProvider;
import com.zerobase.homemate.entity.enums.UserRole;
import com.zerobase.homemate.entity.enums.UserStatus;
import com.zerobase.homemate.repository.UserRepository;
import com.zerobase.homemate.repository.UserSocialAccountRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KakaoLoginTransactionTest {
  private final JwtService jwtService = mock(JwtService.class);
  private final UserRepository userRepository = mock(UserRepository.class);
  private final UserSocialAccountRepository socialRepo = mock(UserSocialAccountRepository.class);

  private final KakaoLoginTransaction sut =
      new KakaoLoginTransaction(jwtService, userRepository, socialRepo);

  @Test
  @DisplayName("신규 가입: User + SocialAccount 생성, 토큰 발급, isNewUser=true")
  void upsert_new_user_then_issue_tokens() {
    // given
    var profile = profile("12345", "Nick", "https://img");

    given(socialRepo.findBySocialProviderAndProviderUserId(SocialProvider.KAKAO, "12345"))
        .willReturn(Optional.empty());

    given(userRepository.save(any(User.class)))
        .willAnswer(inv -> {
          User u = inv.getArgument(0);
          u.setId(1L);
          return u;
        });
    given(socialRepo.save(any(UserSocialAccount.class)))
        .willAnswer(inv -> inv.getArgument(0));

    // JWT 발급
    given(jwtService.createAccessToken(any(User.class))).willReturn("ourAT");
    given(jwtService.createRefreshToken(eq(1L))).willReturn("ourRT");
    given(jwtService.getAccessTokenValiditySeconds()).willReturn(900L);
    given(jwtService.getRefreshTokenValiditySeconds()).willReturn(1_209_600L);

    // when
    SocialLoginDto.LoginResponse res = sut.upsertAndIssue(profile);

    // then
    assertThat(res.tokenType()).isEqualTo("Bearer");
    assertThat(res.accessToken()).isEqualTo("ourAT");
    assertThat(res.refreshToken()).isEqualTo("ourRT");
    assertThat(res.user().id()).isEqualTo(1L);
    assertThat(res.user().provider()).isEqualTo(SocialProvider.KAKAO);
    assertThat(res.user().providerUserId()).isEqualTo("12345");
    assertThat(res.user().nickname()).isEqualTo("Nick");
    assertThat(res.user().profileImageUrl()).isEqualTo("https://img");
    assertThat(res.user().isNewUser()).isTrue();

    then(userRepository).should().save(any(User.class));
    then(socialRepo).should().save(any(UserSocialAccount.class));
    then(jwtService).should().createAccessToken(
        argThat(u -> u.getId() == 1L && "Nick".equals(u.getProfileName())));
    then(jwtService).should().createRefreshToken(eq(1L));
  }

  @Test
  @DisplayName("재로그인: 기존 link 찾아 user 정보(닉네임/이미지) 업데이트, isNewUser=false")
  void upsert_existing_user_updates_profile() {
    // given
    var existingUser = user(10L, "OldNick", "https://old");
    var link = link(existingUser, SocialProvider.KAKAO, "12345");

    given(socialRepo.findBySocialProviderAndProviderUserId(SocialProvider.KAKAO, "12345"))
        .willReturn(Optional.of(link));

    var profile = profile("12345", "NewNick", "https://new");

    // JWT
    given(jwtService.createAccessToken(existingUser)).willReturn("at");
    given(jwtService.createRefreshToken(10L)).willReturn("rt");
    given(jwtService.getAccessTokenValiditySeconds()).willReturn(900L);
    given(jwtService.getRefreshTokenValiditySeconds()).willReturn(1_209_600L);

    // when
    SocialLoginDto.LoginResponse res = sut.upsertAndIssue(profile);

    // then
    assertThat(res.user().isNewUser()).isFalse();
    assertThat(existingUser.getProfileName()).isEqualTo("NewNick");
    assertThat(existingUser.getProfileImageUrl()).isEqualTo("https://new");
    then(userRepository).should(never()).save(any());
  }

  // helpers
  private KakaoDto.ProfileResponse profile(String kakaoUid, String nick, String img) {
    return new KakaoDto.ProfileResponse(
        Long.valueOf(kakaoUid), // id
        new KakaoDto.ProfileResponse.Properties(nick, img)
    );
  }

  private User user(Long id, String nick, String img) {
    var u = new User();
    u.setId(id);
    u.setProfileName(nick);
    u.setProfileImageUrl(img);
    u.setUserRole(UserRole.USER);
    u.setUserStatus(UserStatus.ACTIVE);
    u.setCreatedAt(LocalDateTime.now());
    u.setUpdatedAt(LocalDateTime.now());
    return u;
  }

  private UserSocialAccount link(User user, SocialProvider p, String providerUid) {
    var l = new UserSocialAccount();
    l.setUser(user);
    l.setUserId(user.getId());
    l.setSocialProvider(p);
    l.setProviderUserId(providerUid);
    l.setConnectedAt(LocalDateTime.now());
    return l;
  }
}
