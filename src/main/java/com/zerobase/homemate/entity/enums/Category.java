package com.zerobase.homemate.entity.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public enum Category {
    AFTER_WORK_THIRTY_MINUTES("퇴근 후 30분 집안일"),
    IMMEDIATELY_THREE_MINUTES("당장 할 수 있는 3분컷 집안일"),
    TEN_MINUTES_CLEANING("10분 루틴 집안일"),
    MISSIONS("미션 달성 집안일");

    private final String categoryName;

    Category(String categoryName) {
        this.categoryName = categoryName;
    }

    // Random 대상 Category 뽑기
    private static final Category[] RANDOM_CANDIDATES =
            Arrays.stream(values())
                    .filter(c -> c != MISSIONS)
                    .toArray(Category[]::new);

    public static Category randomExceptMission() {
        return RANDOM_CANDIDATES[
                ThreadLocalRandom.current().nextInt(RANDOM_CANDIDATES.length)
                ];
    }

}
