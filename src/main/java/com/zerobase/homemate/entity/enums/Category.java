package com.zerobase.homemate.entity.enums;

import lombok.Getter;

@Getter
public enum Category {
    WINTER("겨울철 대청소"),
    WEEKEND_WHOLE_ROUTINE("주말 대청소 루틴"),
    HOTEL_BATHROOM("호텔 화장실 따라잡기"),
    SAFETY_CHECK("안전 점검의 날"),
    APPLIANCE_MAINTENANCE("가전제품 관리하기"),
    TEN_MINUTES_CLEANING("하루 10분 청소하기"),
    MISSIONS("미션 달성 집안일"),
    // 계절 카테고리
    SEASON_SPRING("봄"),
    SEASON_SUMMER("여름"),
    SEASON_AUTUMN("가을"),
    SEASON_WINTER("겨울");

    private final String categoryName;

    Category(String categoryName) {
        this.categoryName = categoryName;
    }

}
