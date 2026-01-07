package com.zerobase.homemate.entity.enums;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;

public enum TimeSlot {
    BEFORE_10(0, 10),
    DAYTIME(10, 18),
    AFTER_6PM(18, 24);

    private final int startHour;
    private final int endHour;

    TimeSlot(int startHour, int endHour) {
        this.startHour = startHour;
        this.endHour = endHour;
    }

    public boolean matches(LocalTime time) {
        int hour = time.getHour();
        return hour >= startHour && hour <= endHour;
    }

    public static TimeSlot from(LocalDateTime dateTime) {
        LocalTime time = dateTime.toLocalTime();
        return Arrays.stream(values())
                .filter(slot -> slot.matches(time))
                .findFirst()
                .orElseThrow(); // 여기까지 오면 설계 오류
    }
}
