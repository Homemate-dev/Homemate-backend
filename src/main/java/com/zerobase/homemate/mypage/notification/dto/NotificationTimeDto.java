package com.zerobase.homemate.mypage.notification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class NotificationTimeDto {
  public record NotiTimeResponse (
      @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
      LocalTime notificationTime,
      LocalDateTime updatedAt
  ) {}
}
