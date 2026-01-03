package com.zerobase.homemate.entity.enums;

public enum RepeatType {
    NONE(1),
    DAILY(2),
    WEEKLY(3),
    MONTHLY(4),
    YEARLY(5);

    private final int order;

    RepeatType(int order) {
        this.order = order;
    }

    public int order() {
        return order;
    }
}
