package com.zerobase.homemate.mypage.notification.service;

import com.zerobase.homemate.entity.UserNotificationSetting;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.mypage.notification.dto.FirstSetupStatusDto.FirstSetupRequest;
import com.zerobase.homemate.mypage.notification.dto.FirstSetupStatusDto.FirstSetupResponse;
import com.zerobase.homemate.mypage.notification.dto.FirstSetupStatusDto.FirstSetupStatusResponse;
import com.zerobase.homemate.repository.UserNotificationSettingRepository;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyPageNotificationService {
  private final UserNotificationSettingRepository userNotificationSettingRepository;

  @Transactional(readOnly = true)
  public FirstSetupStatusResponse getFirstSetupStatus(long userId) {
    UserNotificationSetting setting = userNotificationSettingRepository.findByUserId(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOTIFICATION_SETTING_NOT_FOUND));

    return new FirstSetupStatusResponse(
        setting.isFirstSetupCompleted(), setting.getNotificationTime().truncatedTo(ChronoUnit.MINUTES));
  }

  @Transactional
  public FirstSetupResponse completeFirstSetup(long userId, LocalTime time) {
    UserNotificationSetting setting = userNotificationSettingRepository.findByUserId(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOTIFICATION_SETTING_NOT_FOUND));

    if (setting.isFirstSetupCompleted()) {
      throw new CustomException(ErrorCode.FIRST_SETUP_ALREADY_COMPLETED);
    }

    setting.completeFirstSetup(time);

    return FirstSetupResponse.from(setting);
  }
}
