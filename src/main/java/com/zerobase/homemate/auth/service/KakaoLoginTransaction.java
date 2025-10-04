package com.zerobase.homemate.auth.service;

import com.zerobase.homemate.auth.dto.SocialLoginDto;
import com.zerobase.homemate.auth.kakao.KakaoDto.ProfileResponse;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.UserSocialAccount;
import com.zerobase.homemate.entity.enums.SocialProvider;
import com.zerobase.homemate.entity.enums.UserRole;
import com.zerobase.homemate.entity.enums.UserStatus;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.repository.UserRepository;
import com.zerobase.homemate.repository.UserSocialAccountRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KakaoLoginTransaction {
  private final JwtService jwtService;
  private final UserRepository userRepository;
  private final UserSocialAccountRepository socialAccountRepository;

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
    } else {
      // 신규 유저 + 소셜 계정 생성
      user = User.builder()
          .profileName(nickname)
          .profileImageUrl(profileImage)
          .userRole(UserRole.USER)
          .userStatus(UserStatus.ACTIVE)
          .lastLoginAt(now)
          .build();
      userRepository.save(user);

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
    final String at;
    final String rt;
    try {
      at = jwtService.createAccessToken(user);
      rt = jwtService.createRefreshToken(user.getId());
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
