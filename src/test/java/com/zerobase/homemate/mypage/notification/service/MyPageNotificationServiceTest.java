package com.zerobase.homemate.mypage.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.UserNotificationSetting;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.repository.UserNotificationSettingRepository;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MyPageNotificationServiceTest {
  @Mock
  UserNotificationSettingRepository settingsRepo;

  @InjectMocks
  MyPageNotificationService sut;

  @Test
  @DisplayName("최초 알림 시간 설정 여부 조회 성공: 설정이 있으면 firstSetupCompleted/시간 반환")
  void getFirstSetupStatus_ok() {
    // given
    long userId = 10L;
    var settings = UserNotificationSetting.builder()
        .user(User.builder().build())
        .firstSetupCompleted(false)
        .masterEnabled(true)
        .houseworkEnabled(true)
        .noticeEnabled(true)
        .notificationTime(LocalTime.of(9, 0))
        .build();
    given(settingsRepo.findByUserId(userId)).willReturn(Optional.of(settings));

    // when
    var res = sut.getFirstSetupStatus(userId);

    // then
    assertThat(res.firstSetupCompleted()).isFalse();
    assertThat(res.defaultTime()).isEqualTo("09:00");
    then(settingsRepo).should().findByUserId(userId);
    then(settingsRepo).shouldHaveNoMoreInteractions();
  }

  @Test
  @DisplayName("최초 알림 시간 설정 여부 조회 실패: 설정이 없을 시 404 예외 발생")
  void getFirstSetupStatus_notFound() {
    // given
    long userId = 99L;
    given(settingsRepo.findByUserId(userId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> sut.getFirstSetupStatus(userId))
        .isInstanceOf(CustomException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.USER_NOTIFICATION_SETTING_NOT_FOUND);

    then(settingsRepo).should().findByUserId(userId);
    then(settingsRepo).shouldHaveNoMoreInteractions();
  }
}
