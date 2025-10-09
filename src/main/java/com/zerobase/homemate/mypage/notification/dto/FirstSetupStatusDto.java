package com.zerobase.homemate.mypage.notification.dto;

public class FirstSetupStatusDto {
  public record FirstSetupStatusResponse(
      boolean firstSetupCompleted,
      String defaultTime
  ) {}
}
