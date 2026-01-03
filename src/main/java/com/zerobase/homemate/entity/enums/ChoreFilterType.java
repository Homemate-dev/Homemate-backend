package com.zerobase.homemate.entity.enums;

import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import java.util.Arrays;

public enum ChoreFilterType {
    ALL,
    SPACE,
    REPEAT;

    public static ChoreFilterType from (String value) {
        return Arrays.stream(values())
            .filter(v -> v.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new CustomException(ErrorCode.URI_NOT_FOUND));
    }
}
