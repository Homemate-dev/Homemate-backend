package com.zerobase.homemate.mypage.notification.model;

import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;

public enum NotificationType {
  MASTER,
  CHORE,
  NOTICE;

  public static NotificationType from(String type) {
    try {
      return NotificationType.valueOf(type.toUpperCase());
    } catch (Exception e) {
      throw new CustomException(ErrorCode.INVALID_NOTIFICATION_TYPE);
    }
  }
}
