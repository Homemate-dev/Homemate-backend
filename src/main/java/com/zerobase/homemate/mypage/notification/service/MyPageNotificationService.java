package com.zerobase.homemate.mypage.notification.service;

import com.zerobase.homemate.badge.service.BadgeService;
import com.zerobase.homemate.entity.UserNotificationSetting;
import com.zerobase.homemate.entity.enums.BadgeType;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.mypage.notification.dto.FirstSetupStatusDto.FirstSetupResponse;
import com.zerobase.homemate.mypage.notification.dto.FirstSetupStatusDto.FirstSetupStatusResponse;
import com.zerobase.homemate.mypage.notification.dto.NotificationSettingDto.ToggleResponse;
import com.zerobase.homemate.mypage.notification.dto.NotificationTimeDto.NotiTimeResponse;
import com.zerobase.homemate.mypage.notification.model.NotificationType;
import com.zerobase.homemate.repository.UserNotificationSettingRepository;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyPageNotificationService {
  private final UserNotificationSettingRepository userNotificationSettingRepository;
  private final BadgeService badgeService;

  @Transactional(readOnly = true)
  public FirstSetupStatusResponse getFirstSetupStatus(long userId) {
    return FirstSetupStatusResponse.from(getSettingOrThrow(userId));
  }
  
  @Transactional
  public FirstSetupResponse completeFirstSetup(long userId, LocalTime time) {
    UserNotificationSetting setting = getSettingOrThrow(userId);

    if (setting.isFirstSetupCompleted()) {
      throw new CustomException(ErrorCode.FIRST_SETUP_ALREADY_COMPLETED);
    }

    setting.completeFirstSetup(truncateToMinutes(time));
    userNotificationSettingRepository.flush();


    return FirstSetupResponse.from(setting);
  }
  
  @Transactional(readOnly = true)
  public NotiTimeResponse getNotificationTime(long userId) {
    return NotiTimeResponse.from(getSettingOrThrow(userId));
  }

  @Transactional
  public NotiTimeResponse updateNotificationTime(long userId, LocalTime time) {
    UserNotificationSetting setting = getSettingOrThrow(userId);
    LocalTime normalizedTime = truncateToMinutes(time);

    if (!Objects.equals(truncateToMinutes(setting.getNotificationTime()), normalizedTime)) {
      setting.changeNotificationTime(normalizedTime);
      userNotificationSettingRepository.flush();
    }
    return NotiTimeResponse.from(setting);
  }

  @Transactional
  public ToggleResponse toggleNotification(long userId, NotificationType type, boolean enabled) {
    UserNotificationSetting setting = getSettingOrThrow(userId);

    boolean wasMasterEnabled = setting.isMasterEnabled();
    boolean wasNoticeEnabled = setting.isNoticeEnabled();
    boolean wasChoiceEnabled = setting.isChoreEnabled();

    boolean changed = false;

    switch (type) {
      case MASTER -> {
        if (setting.isMasterEnabled() != enabled) {
          setting.applyMasterEnabled(enabled);
          changed = true;
        }
      }
      case CHORE -> {
        if (setting.isChoreEnabled() != enabled) {
          setting.changeChoreEnabled(enabled);
          changed = true;
        }
        syncMaster(setting);
      }
      case NOTICE ->  {
        if (setting.isNoticeEnabled() != enabled) {
          setting.changeNoticeEnabled(enabled);
          changed = true;
        }
        syncMaster(setting);
      }
    }

    if (!changed) {
      return ToggleResponse.from(setting, Optional.empty());
    }
    userNotificationSettingRepository.flush();

    boolean isMasterEnabled = setting.isMasterEnabled();
    boolean isNoticeEnabled = setting.isNoticeEnabled();
    boolean isChoiceEnabled = setting.isChoreEnabled();

    boolean alarmTurnedOn = (!wasMasterEnabled && isMasterEnabled
            || !wasChoiceEnabled && isChoiceEnabled
            || !wasNoticeEnabled && isNoticeEnabled);

    Optional<BadgeType> newBadge = alarmTurnedOn ?
            badgeService.evaluateBadgesOnAlarm(setting.getUser()) : Optional.empty();

    return ToggleResponse.from(setting, newBadge);
  }

  private UserNotificationSetting getSettingOrThrow(long userId) {
    return userNotificationSettingRepository.findByUserId(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOTIFICATION_SETTING_NOT_FOUND));
  }

  private static LocalTime truncateToMinutes(LocalTime time) {
    return time.truncatedTo(ChronoUnit.MINUTES);
  }

  // chore/notice 두 값이 같아질 때, master가 다르다면 같은 값으로 동기화
  private void syncMaster(UserNotificationSetting s) {
    boolean chore = s.isChoreEnabled();
    boolean notice = s.isNoticeEnabled();

    if (chore == notice && s.isMasterEnabled() != chore) {
      s.changeMasterEnabled(chore);
    }
  }
}
