package com.zerobase.homemate.entity.enums;

import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;

public enum NotificationCategory {
    CHORE,
    NOTICE;

    // valueOf에서 발생하는 예외를 CustomException으로 전환
    public static NotificationCategory from(String value) {
        try {
            return NotificationCategory.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR, "잘못된 카테고리 입력입니다.");
        }
    }
}
