package com.zerobase.homemate.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BadgeType {

    START_HALF( "시작이 반", "아무 집안일 1회 완료", null, null,  null, 1, false, false),

    SEED_CHORE( "새싹 살림꾼", "아무 집안일 100회 완료", null, null,null, 100, false, false),
    MEDIUM_CHORE( "알뜰 살림꾼", "아무 집안일 200회 완료", null, null, null,200, false, false),
    MASTER_CHORE( "살림 마스터", "아무 집안일 300회 완료", null, null, null,300 , false, false),

    SMALL_J("소문자 J", "집안일 30회 등록하기", null, null, null,30, true, false),
    LARGE_J( "대문자 J", "집안일 90회 등록하기", null, null, null,90, true, false),
    POWER_J( "파워 J", "집안일 180회 등록하기", null, null, null,180, true, false),

    SEED_MISSION( "미션 새싹", "미션 3회 달성하기", null, null, null,3, true, false),
    EXPERT_MISSION( "미션 달인", "미션 18회 달성하기", null, null, null,18, true, false),
    MASTER_MISSION( "미션 마스터", "미션 36회 달성하기", null, null, null,36, true, false),

    BEGINNER_KITCHEN("주방 깔끔이", "주방 집안일 30회 완료", null, Space.KITCHEN, null,30, false, false),
    EXPERT_KITCHEN("주방 반짝이", "주방 집안일 90회 완료", null, Space.KITCHEN, null,90, false, false),
    MASTER_KITCHEN("주방 번쩍이", "주방 집안일 180회 완료", null, Space.KITCHEN, null,180, false, false),

    BEGINNER_BATHROOM("욕실 깔끔이", "욕실 집안일 30회 완료", null, Space.BATHROOM, null,30, false, false),
    EXPERT_BATHROOM("욕실 반짝이", "욕실 집안일 90회 완료", null, Space.BATHROOM, null,90, false, false),
    MASTER_BATHROOM("욕실 번쩍이", "욕실 집안일 180회 완료", null, Space.BATHROOM, null,180, false, false),

    BEGINNER_PORCH("현관 깔끔이", "현관 집안일 30회 완료", null, Space.PORCH, null,30, false, false),
    EXPERT_PORCH("현관 반짝이", "현관 집안일 90회 완료", null,  Space.PORCH, null,90, false, false),
    MASTER_PORCH("현관 번쩍이", "현관 집안일 180회 완료", null,  Space.PORCH, null,180, false, false),

    SEED_LAUNDRY("뽀송 새싹", "빨래하기 30회 완료", null, null, "빨래하기", 30, false, false),
    EXPERT_LAUNDRY("뽀송 달인", "빨래하기 90회 완료", null, null, "빨래하기", 90, false, false),
    MASTER_LAUNDRY("뽀송 마스터", "빨래하기 180회 완료", null, null, "빨래하기", 180, false, false),

    WATER_SPOTS_ERASER("물때 지우개", "거울/수전 물때 닦기 30회 완료", null, null, "거울/수전 물때 닦기", 30, false, false),
    WATER_SPOTS_HUNTER("물때 사냥꾼", "거울/수전 물때 닦기 90회 완료", null, null, "거울/수전 물때 닦기", 90, false, false),
    WATER_SPOTS_DESTROYER("물때 박멸자", "거울/수전 물때 닦기 180회 완료", null, null, "거울/수전 물때 닦기", 180, false, false),

    CHECK_FIRE_EXHAUSTER("우리집 소방관", "소화기 점검 2회 완료", null, null, "소화기 점검", 2, false, false),

    BEGINNER_FAIRY("쓱쓱요정", "바닥 청소기 30회 완료", null, null, "바닥 청소기 돌리기", 30, false, false),
    EXPERT_FAIRY("싹싹요정", "바닥 청소기 90회 완료", null,null,"바닥 청소기 돌리기", 90, false, false),
    MASTER_FAIRY("쓱싹요정", "바닥 청소기 180회 완료", null, null, "바닥 청소기 돌리기", 180, false, false),

    BEGINNER_MORNING("상쾌한 모닝", "기상 후 침구 정리하기 30회 완료", null, null, "기상 후 침구 정리하기", 30, false, false),
    EXPERT_MORNING("개운한 모닝", "기상 후 침구 정리하기 90회 완료", null, null, "기상 후 침구 정리하기", 90, false, false),
    MASTER_MORNING("미라클 모닝", "기상 후 침구 정리하기 180회 완료", null, null, "기상 후 침구 정리하기", 180, false, false),

    BEGINNER_TRASH_BIN("쓰레기 텅", "쓰레기통 비우기 30회 완료", null, null, "쓰레기통 비우기", 30, false, false),
    EXPERT_TRASH_BIN("쓰레기 텅텅", "쓰레기통 비우기 90회 완료", null, null, "쓰레기통 비우기", 90, false, false),
    MASTER_TRASH_BIN("텅텅 비움이", "쓰레기통 비우기 180회 완료", null, null, "쓰레기통 비우기", 180, false, false);


    private final String badgeName;
    private final String description;
    private final Category category;
    private final Space space;
    private final String choreTitle;
    private final int requireCount;
    private final boolean isRegisterBadge;
    private final boolean isMissionBadge;

}
