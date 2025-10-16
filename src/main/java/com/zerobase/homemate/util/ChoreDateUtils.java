package com.zerobase.homemate.util;

import com.zerobase.homemate.entity.enums.RepeatType;

import java.time.LocalDate;


public class ChoreDateUtils {

    private ChoreDateUtils() {} // 인스턴스화 방지

    public static LocalDate calculateEndDate(LocalDate startDate, RepeatType type, int interval) {
        return switch (type) {
            case NONE -> startDate;
            case DAILY -> startDate.plusDays(interval);
            case WEEKLY -> startDate.plusWeeks(interval);
            case MONTHLY -> startDate.plusMonths(interval);
            case YEARLY -> startDate.plusYears(interval);
        };
    }
}
