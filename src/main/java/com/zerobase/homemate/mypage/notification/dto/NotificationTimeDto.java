package com.zerobase.homemate.mypage.notification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zerobase.homemate.entity.UserNotificationSetting;
import com.zerobase.homemate.entity.enums.BadgeType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

public class NotificationTimeDto {
  public record NotiTimeResponse (
      @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
      LocalTime notificationTime,
      LocalDateTime updatedAt,
      Optional<BadgeType> newBadge
  ) {
    public static NotiTimeResponse from(UserNotificationSetting s, Optional<BadgeType> newBadge) {
      return new NotiTimeResponse(
          s.getNotificationTime(),
          s.getUpdatedAt(),
              newBadge
      );
    }
  }

  public record NotiTimeRequest (
      @NotNull
      @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
      LocalTime notificationTime
  ) {}
}
