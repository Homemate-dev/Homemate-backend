package com.zerobase.homemate.dev;

import com.zerobase.homemate.auth.service.JwtService;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@ConditionalOnProperty(
    prefix = "auth.dev",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = false
)
@RestController
@RequestMapping("/auth/dev")
@RequiredArgsConstructor
public class DevAuthController {
  private final JwtService jwtService;
  private final RefreshTokenStore refreshTokenStore;
  private final UserRepository userRepository;
  private final UserSocialAccountRepository socialAccountRepository;
  private final UserNotificationSettingRepository notificationSettingRepository;

  // 개발/테스트 전용 토큰 발급 엔드포인트
  @PostMapping("/token/{id}")
  public ResponseEntity<Map<String, String>> token(@PathVariable Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    String sid = UUID.randomUUID().toString();
    String at = jwtService.createAccessToken(user, sid);
    String rt = jwtService.createRefreshToken(user.getId(), sid);
    refreshTokenStore.save(user.getId(), sid, jwtService.getJti(rt));

    Map<String, String> response = new LinkedHashMap<>();

    response.put("tokenType", "Bearer");
    response.put("accessToken", at);
    response.put("refreshToken", rt);

    return ResponseEntity.ok(response);
  }

  @PostMapping("/signup")
  public ResponseEntity<Map<String, Long>> signup() {
    // 신규 유저 + 유저 알림 설정(Default) + 소셜 링크 생성
    User user = User.builder()
        .profileName("nickname")
        .profileImageUrl("profileImage")
        .userRole(UserRole.USER)
        .userStatus(UserStatus.ACTIVE)
        .lastLoginAt(LocalDateTime.now())
        .build();
    userRepository.saveAndFlush(user);

    notificationSettingRepository.save(
        UserNotificationSetting.createDefault(user, LocalTime.of(9, 0)));

    UserSocialAccount link = UserSocialAccount.builder()
        .user(user)
        .socialProvider(SocialProvider.KAKAO)
        .providerUserId("kakaoUid")
        .connectedAt(LocalDateTime.now())
        .build();

    try {
      socialAccountRepository.saveAndFlush(link);
    } catch (DataIntegrityViolationException e) {
      throw new CustomException(ErrorCode.SOCIAL_LINK_CONFLICT);
    }

    Map<String, Long> response = new LinkedHashMap<>();

    response.put("id", user.getId());

    return ResponseEntity.ok(response);
  }
}
