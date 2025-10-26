package com.zerobase.homemate.notification.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum NotificationIndex {
    T_MINUS_1D("Tminus1d"),
    T_MINUS_10M("Tminus10m"),
    T_0("T0");

    private final String index;

    public String index() {
        return index;
    }

    public static NotificationIndex fromIndex(String index) {
        for (NotificationIndex i : NotificationIndex.values()) {
            if (i.index.equals(index)) {
                return i;
            }
        }

        return null;
    }
}
