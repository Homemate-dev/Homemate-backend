package com.zerobase.homemate.mypage.notification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zerobase.homemate.entity.UserNotificationSetting;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class FirstSetupStatusDto {
  public record FirstSetupStatusResponse(
      boolean firstSetupCompleted,
      @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
      LocalTime notificationTime
  ) {}

  public record FirstSetupRequest (
      @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
      @NotNull
      LocalTime notificationTime
  ) {}

  public record FirstSetupResponse(
      boolean firstSetupCompleted,
      boolean masterEnabled,
      @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
      LocalTime notificationTime,
      LocalDateTime updatedAt
  ) {
    public static FirstSetupResponse from(UserNotificationSetting s) {
      return new FirstSetupResponse(
          s.isFirstSetupCompleted(),
          s.isMasterEnabled(),
          s.getNotificationTime(),
          s.getUpdatedAt()
      );
    }
  }
}
