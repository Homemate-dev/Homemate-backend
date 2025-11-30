package com.zerobase.homemate.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BadgeType {

    START_HALF( "시작이 반", "아무 집안일 1회 완료",  null,  null, 1, BadgeCategory.ALL, "start_half.png"),

    SEED_CHORE( "새싹 살림꾼", "아무 집안일 100회 완료",  null,null, 100, BadgeCategory.ALL, "beginner_all.png"),
    MEDIUM_CHORE( "알뜰 살림꾼", "아무 집안일 200회 완료",  null, null,200, BadgeCategory.ALL, "expert_all.png"),
    MASTER_CHORE( "살림 마스터", "아무 집안일 300회 완료",  null, null,300 , BadgeCategory.ALL, "master_all.png"),

    SMALL_J("소문자 J", "집안일 30회 등록하기",  null, null,30, BadgeCategory.REGISTER, "beginner_j.png"),
    LARGE_J( "대문자 J", "집안일 90회 등록하기", null, null,90, BadgeCategory.REGISTER, "expert_j.png"),
    POWER_J( "파워 J", "집안일 180회 등록하기",  null, null,180, BadgeCategory.REGISTER, "master_j.png"),

    SEED_MISSION( "미션 새싹", "미션 3회 달성하기",  null, null,3, BadgeCategory.MISSION, "beginner_seed.png"),
    EXPERT_MISSION( "미션 달인", "미션 18회 달성하기",  null, null,18, BadgeCategory.MISSION, "expert_seed.png"),
    MASTER_MISSION( "미션 마스터", "미션 36회 달성하기",  null, null,36, BadgeCategory.MISSION, "master_seed.png"),

    BEGINNER_KITCHEN("주방 깔끔이", "주방 집안일 30회 완료",  Space.KITCHEN, null,30, BadgeCategory.SPACE, "beginner_kitchen.png"),
    EXPERT_KITCHEN("주방 반짝이", "주방 집안일 90회 완료",  Space.KITCHEN, null,90, BadgeCategory.SPACE, "expert_kitchen.png"),
    MASTER_KITCHEN("주방 번쩍이", "주방 집안일 180회 완료",  Space.KITCHEN, null,180, BadgeCategory.SPACE, "master_kitchen.png"),

    BEGINNER_BATHROOM("욕실 깔끔이", "욕실 집안일 30회 완료",  Space.BATHROOM, null,30, BadgeCategory.SPACE, "beginner_bathroom.png"),
    EXPERT_BATHROOM("욕실 반짝이", "욕실 집안일 90회 완료",  Space.BATHROOM, null,90, BadgeCategory.SPACE, "expert_bathroom.png"),
    MASTER_BATHROOM("욕실 번쩍이", "욕실 집안일 180회 완료",  Space.BATHROOM, null,180, BadgeCategory.SPACE, "master_bathroom.png"),

    BEGINNER_PORCH("현관 깔끔이", "현관 집안일 30회 완료",  Space.PORCH, null,30, BadgeCategory.SPACE, "beginner_porch.png"),
    EXPERT_PORCH("현관 반짝이", "현관 집안일 90회 완료",   Space.PORCH, null,90, BadgeCategory.SPACE, "expert_porch.png"),
    MASTER_PORCH("현관 번쩍이", "현관 집안일 180회 완료",   Space.PORCH, null,180, BadgeCategory.SPACE, "master_porch.png"),

    SEED_LAUNDRY("뽀송 새싹", "빨래하기 30회 완료",  null, "빨래하기", 30, BadgeCategory.TITLE, "beginner_laundry.png"),
    EXPERT_LAUNDRY("뽀송 달인", "빨래하기 90회 완료",  null, "빨래하기", 90, BadgeCategory.TITLE, "expert_laundry.png"),
    MASTER_LAUNDRY("뽀송 마스터", "빨래하기 180회 완료", null, "빨래하기", 180, BadgeCategory.TITLE, "master_laundry.png"),

    WATER_SPOTS_ERASER("물때 지우개", "거울/수전 물때 닦기 30회 완료",  null, "거울/수전 물때 닦기", 30, BadgeCategory.TITLE, "beginner_mirror.png"),
    WATER_SPOTS_HUNTER("물때 사냥꾼", "거울/수전 물때 닦기 90회 완료",  null, "거울/수전 물때 닦기", 90, BadgeCategory.TITLE, "expert_mirror.png"),
    WATER_SPOTS_DESTROYER("물때 박멸자", "거울/수전 물때 닦기 180회 완료", null, "거울/수전 물때 닦기", 180, BadgeCategory.TITLE, "master_mirror.png"),

    CHECK_FIRE_EXHAUSTER("우리집 소방관", "소화기 점검 2회 완료",  null, "소화기 점검하기", 2, BadgeCategory.TITLE, "beginner_fire_exhauster.png"),

    BEGINNER_FAIRY("쓱쓱요정", "바닥 청소기 30회 완료",  null, "바닥 청소기 돌리기", 30, BadgeCategory.TITLE, "beginner_vacuum.png"),
    EXPERT_FAIRY("싹싹요정", "바닥 청소기 90회 완료", null,"바닥 청소기 돌리기", 90, BadgeCategory.TITLE, "expert_vacuum.png"),
    MASTER_FAIRY("쓱싹요정", "바닥 청소기 180회 완료", null, "바닥 청소기 돌리기", 180, BadgeCategory.TITLE, "master_vacuum.png"),

    BEGINNER_MORNING("상쾌한 모닝", "기상 후 침구 정리하기 30회 완료",  null, "기상 후 침구 정리하기", 30, BadgeCategory.TITLE, "beginner_arrange_bed.png"),
    EXPERT_MORNING("개운한 모닝", "기상 후 침구 정리하기 90회 완료",  null, "기상 후 침구 정리하기", 90, BadgeCategory.TITLE, "expert_arrange_bed.png"),
    MASTER_MORNING("미라클 모닝", "기상 후 침구 정리하기 180회 완료",  null, "기상 후 침구 정리하기", 180, BadgeCategory.TITLE, "master_arrange_bed.png"),

    BEGINNER_TRASH_BIN("쓰레기 텅", "쓰레기통 비우기 30회 완료",  null, "쓰레기통 비우기", 30, BadgeCategory.TITLE, "beginner_empty_trash.png"),
    EXPERT_TRASH_BIN("쓰레기 텅텅", "쓰레기통 비우기 90회 완료",  null, "쓰레기통 비우기", 90, BadgeCategory.TITLE, "expert_empty_trash.png"),
    MASTER_TRASH_BIN("텅텅 비움이", "쓰레기통 비우기 180회 완료",  null, "쓰레기통 비우기", 180, BadgeCategory.TITLE, "master_empty_trash.png"),

    BEGINNER_BEDROOM("침실 깔끔이", "침실 집안일 30회 완료", Space.BEDROOM, null, 30, BadgeCategory.SPACE, "beginner_bedroom.png"),
    EXPERT_BEDROOM("침실 반짝이", "침실 집안일 90회 완료", Space.BEDROOM, null, 90, BadgeCategory.SPACE, "expert_bedroom.png"),
    MASTER_BEDROOM("침실 번쩍이", "침실 집안일 180회 완료", Space.BEDROOM, null, 180, BadgeCategory.SPACE, "master_bedroom.png");


    private final String badgeName;
    private final String description;
    private final Space space;
    private final String choreTitle;
    private final int requireCount;
    private final BadgeCategory category;
    private final String imageName;

    private static final String BASE_URL = "https://homemate.io.kr/badges/";

    public String getBadgeImageUrl(){
        return BASE_URL + imageName;
    }

}
