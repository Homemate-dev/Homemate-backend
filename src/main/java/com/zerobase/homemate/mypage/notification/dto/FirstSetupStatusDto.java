package com.zerobase.homemate.mypage.notification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

public class FirstSetupStatusDto {
  public record FirstSetupStatusResponse(
      boolean firstSetupCompleted,
      @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
      LocalTime defaultTime
  ) {}
}
