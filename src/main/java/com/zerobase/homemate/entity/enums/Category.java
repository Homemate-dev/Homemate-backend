package com.zerobase.homemate.entity.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {

    WINTER("겨울맞이 대청소"),
    WEEKEND("주말 대청소 루틴"),
    TOILET("호텔 화장실 따라잡기"),
    SAFE("안전 점검의 날"),
    HOME_APPLIANCES("가전제품 관리하기"),
    DAILY_ROUTINE_TEN("하루 10분 청소하기");



    private final String nameKo;
}
