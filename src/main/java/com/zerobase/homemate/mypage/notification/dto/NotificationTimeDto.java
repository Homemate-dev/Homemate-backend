package com.zerobase.homemate.mypage.notification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class NotificationTimeDto {
  public record NotiTimeResponse (
      @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
      LocalTime notificationTime,
      LocalDateTime updatedAt
  ) {}

  public record NotiTimeRequest (
      @NotNull
      @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
      LocalTime notificationTime
  ) {}
}
