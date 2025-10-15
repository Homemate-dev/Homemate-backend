package com.zerobase.homemate.mypage.notification.dto;

import com.zerobase.homemate.entity.UserNotificationSetting;
import java.time.LocalDateTime;

public class NotificationSettingDto {
  public record ToggleRequest(
      boolean enabled
  ) {}

  public record ToggleResponse(
      boolean masterEnabled,
      boolean choreEnabled,
      boolean noticeEnabled,
      LocalDateTime updatedAt
  ) {
    public static ToggleResponse from(UserNotificationSetting s) {
      return new ToggleResponse(
          s.isMasterEnabled(),
          s.isChoreEnabled(),
          s.isNoticeEnabled(),
          s.getUpdatedAt()
      );
    }
  }
}
