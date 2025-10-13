package com.zerobase.homemate.mypage.notification.dto;

import com.zerobase.homemate.entity.UserNotificationSetting;
import java.time.LocalDateTime;

public class NotificationSettingDto {
  public record MasterToggleRequest (
      boolean enabled
  ) {}

  public record MasterToggleResponse (
      long id,
      boolean masterEnabled,
      boolean choreEnabled,
      boolean noticeEnabled,
      LocalDateTime updatedAt
  ) {
    public static MasterToggleResponse from(UserNotificationSetting s) {
      return new MasterToggleResponse(
          s.getId(),
          s.isMasterEnabled(),
          s.isChoreEnabled(),
          s.isNoticeEnabled(),
          s.getUpdatedAt()
      );
    }
  }
}
