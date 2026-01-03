package com.zerobase.homemate.entity.enums;

import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;

import java.util.Arrays;

public enum RepeatType {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY;


    public static RepeatType from(String value) {
        return Arrays.stream(values())
                .filter(v -> v.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.URI_NOT_FOUND));
    }
}
