package com.zerobase.homemate.entity.enums;

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
        int month = date.getMonthValue();

        for (Season season : values()) {
            if (season.contains(month)) {
                return season;
            }
        }

        // 논리적으로는 도달할 수 없는 위치
        return WINTER;
    }

    private boolean contains(int month) {
        if (startMonth <= endMonth) {
            return month >= startMonth && month <= endMonth;
        }
        // 겨울처럼 연도 넘는 경우
        return month >= startMonth || month <= endMonth;
    }
}
