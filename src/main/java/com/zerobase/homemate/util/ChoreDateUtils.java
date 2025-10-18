package com.zerobase.homemate.util;

import com.zerobase.homemate.entity.enums.RepeatType;

import java.time.LocalDate;


public class ChoreDateUtils {

    private ChoreDateUtils() {} // 인스턴스화 방지

    public static LocalDate calculateEndDate(LocalDate startDate, RepeatType type, int interval) {
        return switch (type) {
            case NONE -> startDate;
            case DAILY, WEEKLY -> startDate.plusWeeks(4);
            case MONTHLY -> {
                if(interval == 1) yield startDate.plusMonths(3);
                else if(interval <= 3) yield startDate.plusYears(1);
                else yield startDate.plusYears(2);
            }
            case YEARLY -> startDate.plusYears(3);
        };
    }
}
