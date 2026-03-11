package com.zerobase.homemate.mypage.notification.dto;

import com.zerobase.homemate.entity.UserNotificationSetting;
import com.zerobase.homemate.entity.enums.BadgeType;

import java.time.LocalDateTime;
import java.util.Optional;

public class NotificationSettingDto {
  public record ToggleRequest(
      boolean enabled
  ) {}

  public record ToggleResponse(
      boolean masterEnabled,
      boolean choreEnabled,
      boolean noticeEnabled,
      LocalDateTime updatedAt,
      Optional<BadgeType> newBadge
  ) {
    public static ToggleResponse from(UserNotificationSetting s, Optional<BadgeType> newBadge) {
      return new ToggleResponse(
          s.isMasterEnabled(),
          s.isChoreEnabled(),
          s.isNoticeEnabled(),
          s.getUpdatedAt(),
              newBadge
      );
    }
  }
}
