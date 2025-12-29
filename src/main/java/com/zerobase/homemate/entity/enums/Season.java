package com.zerobase.homemate.entity.enums;

import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;

import java.time.LocalDate;

public enum Season {
    SPRING(3, 5),
    SUMMER(6, 8),
    AUTUMN(9, 11),
    WINTER(12, 2);

    private final int startMonth;
    private final int endMonth;

    Season(int startMonth, int endMonth) {
        this.startMonth = startMonth;
        this.endMonth = endMonth;
    }

    public static Season from(LocalDate date) {
        int m = date.getMonthValue();

        for (Season season : values()) {
            if (season.contains(m)) {
                return season;
            }
        }
        throw new CustomException(ErrorCode.INVALID_DATE_RANGE);
    }

    private boolean contains(int month) {
        if (startMonth <= endMonth) {
            return month >= startMonth && month <= endMonth;
        }
        // 겨울처럼 연도 넘는 경우
        return month >= startMonth || month <= endMonth;
    }
}
