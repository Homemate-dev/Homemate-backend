package com.zerobase.homemate.auth.service;

import com.zerobase.homemate.auth.dto.SocialLoginDto;
import com.zerobase.homemate.auth.kakao.KakaoDto.ProfileResponse;
import com.zerobase.homemate.auth.token.RefreshTokenStore;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.UserNotificationSetting;
import com.zerobase.homemate.entity.UserSocialAccount;
import com.zerobase.homemate.entity.enums.SocialProvider;
import com.zerobase.homemate.entity.enums.UserRole;
import com.zerobase.homemate.entity.enums.UserStatus;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.repository.UserNotificationSettingRepository;
import com.zerobase.homemate.repository.UserRepository;
import com.zerobase.homemate.repository.UserSocialAccountRepository;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KakaoLoginTransaction {
  private static final LocalTime DEFAULT_NOTIFICATION_TIME = LocalTime.of(9, 0);

  private final JwtService jwtService;
  private final RefreshTokenStore refreshTokenStore;
  private final UserRepository userRepository;
  private final UserSocialAccountRepository socialAccountRepository;
  private final UserNotificationSettingRepository notificationSettingRepository;

  @Transactional
  public SocialLoginDto.LoginResponse upsertAndIssue(ProfileResponse profile) {
    LocalDateTime now = LocalDateTime.now();

    String kakaoUid = String.valueOf(profile.id());
    String nickname = profile.properties() != null ? profile.properties().nickname() : null;
    String profileImage = profile.properties() != null ? profile.properties().profileImage() : null;

    // provider + uid로 기존 연결 확인
    Optional<UserSocialAccount> existing =
        socialAccountRepository.findBySocialProviderAndProviderUserId(SocialProvider.KAKAO, kakaoUid);

    boolean isNewUser = existing.isEmpty();
    User user;

    if (existing.isPresent()) {
      // 기존 유저
      user = existing.get().getUser();

      // 비활성/정지 사용자는 로그인 불가
      if (user.getUserStatus() == UserStatus.SUSPENDED) {
        throw new CustomException(ErrorCode.FORBIDDEN, "정지된 사용자입니다.");
      }

      // 유저 정보 최신화
      user.loginAndProfileUpdate(nickname, profileImage, now);

      // 기존 유저인데 알림 설정이 없는 경우가 있을 수 있으니 보정(개발/테스트 계정, 에러 발생 등)
      if (!notificationSettingRepository.existsByUserId(user.getId())) {
        notificationSettingRepository.save(
            UserNotificationSetting.createDefault(user, DEFAULT_NOTIFICATION_TIME));
      }
    } else {
      // 신규 유저 + 유저 알림 설정(Default) + 소셜 링크 생성
      user = User.builder()
          .profileName(nickname)
          .profileImageUrl(profileImage)
          .userRole(UserRole.USER)
          .userStatus(UserStatus.ACTIVE)
          .lastLoginAt(now)
          .build();
      userRepository.save(user);

      notificationSettingRepository.save(
          UserNotificationSetting.createDefault(user, DEFAULT_NOTIFICATION_TIME));

      UserSocialAccount link = UserSocialAccount.builder()
          .user(user)
          .socialProvider(SocialProvider.KAKAO)
          .providerUserId(kakaoUid)
          .connectedAt(now)
          .build();

      try {
        socialAccountRepository.saveAndFlush(link);
      } catch (DataIntegrityViolationException e) {
        throw new CustomException(ErrorCode.SOCIAL_LINK_CONFLICT);
      }
    }

    // 우리 서비스용 JWT 발급
    final String sid = UUID.randomUUID().toString();
    final String at;
    final String rt;
    try {
      long userId = user.getId();
      at = jwtService.createAccessToken(user, sid);
      rt = jwtService.createRefreshToken(userId, sid);
      refreshTokenStore.save(userId, sid, jwtService.getJti(rt));
    } catch (Exception e) {
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    return new SocialLoginDto.LoginResponse(
        "Bearer",
        at,
        jwtService.getAccessTokenValiditySeconds(),
        rt,
        jwtService.getRefreshTokenValiditySeconds(),
        new SocialLoginDto.LoginResponse.UserDto(
            user.getId(),
            SocialProvider.KAKAO,
            kakaoUid,
            user.getProfileName(),
            user.getProfileImageUrl(),
            isNewUser
        )
    );
  }
}
