package com.zerobase.homemate.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BadgeType {

    START_HALF( "시작이 반", "아무 집안일 1회 완료",  null,  null, 1, BadgeCategory.ALL, null,"start_half.png"),

    STREAK_THREE("꾸준이", "아무 집안일이든 연속으로 3일동안 완료하기", null,null, 3, BadgeCategory.STREAK, null,"streak_basic.png"),
    STREAK_FIVE("성실왕", "아무 집안일이든 연속으로 5일동안 완료하기", null,null, 5, BadgeCategory.STREAK, null,"streak_expert.png"),
    STREAK_TEN("끈기왕", "아무 집안일이든 연속으로 10일동안 완료하기", null, null, 10, BadgeCategory.STREAK, null,"streak_master.png"),

    SEED_CHORE( "새싹 살림꾼", "아무 집안일 100회 완료",  null,null, 30, BadgeCategory.ALL, null,"beginner_all.png"),
    MEDIUM_CHORE( "알뜰 살림꾼", "아무 집안일 200회 완료",  null, null,50, BadgeCategory.ALL,null, "expert_all.png"),
    MASTER_CHORE( "살림 마스터", "아무 집안일 300회 완료",  null, null,100 , BadgeCategory.ALL, null,"master_all.png"),

    START_J("예비 J", "집안일 10회 등록하기", null, null, 10, BadgeCategory.REGISTER, null,"pre_start_j.png"),
    SMALL_J("소문자 J", "집안일 30회 등록하기",  null, null,30, BadgeCategory.REGISTER, null,"beginner_j.png"),
    LARGE_J( "대문자 J", "집안일 90회 등록하기", null, null,90, BadgeCategory.REGISTER, null,"expert_j.png"),
    POWER_J( "파워 J", "집안일 180회 등록하기",  null, null,180, BadgeCategory.REGISTER, null,"master_j.png"),

    MIRACLE_MORNING("미라클 모닝", "오전 10시 이전에 집안일 10회 완료하기", null, null, 10, BadgeCategory.TIME, TimeSlot.BEFORE_10,"miracle_morning.png"),
    GOD_LIVE_DINNER("갓생러", "오후 6시 이후에 집안일 10회 완료하기", null, null, 10, BadgeCategory.TIME, TimeSlot.AFTER_6PM,"GOD_LIVE.png"),

    SEED_MISSION( "미션 새싹", "미션 3회 달성하기",  null, null,3, BadgeCategory.MISSION, null,"beginner_seed.png"),
    EXPERT_MISSION( "미션 달인", "미션 18회 달성하기",  null, null,18, BadgeCategory.MISSION, null,"expert_seed.png"),
    MASTER_MISSION( "미션 마스터", "미션 36회 달성하기",  null, null,36, BadgeCategory.MISSION, null,"master_seed.png"),

    BEGINNER_KITCHEN("주방 깔끔이", "주방 집안일 30회 완료",  Space.KITCHEN, null,10, BadgeCategory.SPACE, null,"beginner_kitchen.png"),
    EXPERT_KITCHEN("주방 반짝이", "주방 집안일 90회 완료",  Space.KITCHEN, null,30, BadgeCategory.SPACE, null,"expert_kitchen.png"),
    MASTER_KITCHEN("주방 번쩍이", "주방 집안일 180회 완료",  Space.KITCHEN, null,60, BadgeCategory.SPACE, null,"master_kitchen.png"),

    BEGINNER_BATHROOM("욕실 깔끔이", "욕실 집안일 30회 완료",  Space.BATHROOM, null,10, BadgeCategory.SPACE, null,"beginner_bathroom.png"),
    EXPERT_BATHROOM("욕실 반짝이", "욕실 집안일 90회 완료",  Space.BATHROOM, null,30, BadgeCategory.SPACE, null,"expert_bathroom.png"),
    MASTER_BATHROOM("욕실 번쩍이", "욕실 집안일 180회 완료",  Space.BATHROOM, null,60, BadgeCategory.SPACE, null,"master_bathroom.png"),

    BEGINNER_PORCH("현관 깔끔이", "현관 집안일 30회 완료",  Space.PORCH, null,10, BadgeCategory.SPACE, null,"beginner_porch.png"),
    EXPERT_PORCH("현관 반짝이", "현관 집안일 90회 완료",   Space.PORCH, null,30, BadgeCategory.SPACE, null,"expert_porch.png"),
    MASTER_PORCH("현관 번쩍이", "현관 집안일 180회 완료",   Space.PORCH, null,60, BadgeCategory.SPACE, null,"master_porch.png"),

    SEED_LAUNDRY("뽀송 새싹", "빨래하기 30회 완료",  null, "빨래하기", 30, BadgeCategory.TITLE, null,"beginner_laundry.png"),
    EXPERT_LAUNDRY("뽀송 달인", "빨래하기 90회 완료",  null, "빨래하기", 90, BadgeCategory.TITLE, null,"expert_laundry.png"),
    MASTER_LAUNDRY("뽀송 마스터", "빨래하기 180회 완료", null, "빨래하기", 180, BadgeCategory.TITLE, null,"master_laundry.png"),

    WATER_SPOTS_ERASER("물때 지우개", "거울/수전 물 때 닦기 30회 완료",  null, "거울/수전 물 때 닦기", 30, BadgeCategory.TITLE, null,"beginner_mirror.png"),
    WATER_SPOTS_HUNTER("물때 사냥꾼", "거울/수전 물 때 닦기 90회 완료",  null, "거울/수전 물 때 닦기", 90, BadgeCategory.TITLE, null,"expert_mirror.png"),
    WATER_SPOTS_DESTROYER("물때 박멸자", "거울/수전 물 때 닦기 180회 완료", null, "거울/수전 물 때 닦기", 180, BadgeCategory.TITLE, null,"master_mirror.png"),

    CHECK_FIRE_EXHAUSTER("우리집 소방관", "소화기 점검 2회 완료",  null, "소화기 점검하기", 2, BadgeCategory.TITLE, null,"beginner_fire_exhauster.png"),

    BEGINNER_FAIRY("쓱쓱요정", "바닥 청소기 30회 완료",  null, "바닥 청소기 돌리기", 30, BadgeCategory.TITLE, null,"beginner_vacuum.png"),
    EXPERT_FAIRY("싹싹요정", "바닥 청소기 90회 완료", null,"바닥 청소기 돌리기", 90, BadgeCategory.TITLE, null,"expert_vacuum.png"),
    MASTER_FAIRY("쓱싹요정", "바닥 청소기 180회 완료", null, "바닥 청소기 돌리기", 180, BadgeCategory.TITLE, null,"master_vacuum.png"),

    BEGINNER_MORNING("상쾌한 모닝", "기상 후 침구 정리하기 30회 완료",  null, "기상 후 침구 정리하기", 30, BadgeCategory.TITLE, null,"beginner_arrange_bed.png"),
    EXPERT_MORNING("개운한 모닝", "기상 후 침구 정리하기 90회 완료",  null, "기상 후 침구 정리하기", 90, BadgeCategory.TITLE, null,"expert_arrange_bed.png"),
    MASTER_MORNING("미라클 모닝", "기상 후 침구 정리하기 180회 완료",  null, "기상 후 침구 정리하기", 180, BadgeCategory.TITLE, null,"master_arrange_bed.png"),

    BEGINNER_TRASH_BIN("쓰레기 텅", "쓰레기통 비우기 30회 완료",  null, "쓰레기통 비우기", 30, BadgeCategory.TITLE, null,"beginner_empty_trash.png"),
    EXPERT_TRASH_BIN("쓰레기 텅텅", "쓰레기통 비우기 90회 완료",  null, "쓰레기통 비우기", 90, BadgeCategory.TITLE, null,"expert_empty_trash.png"),
    MASTER_TRASH_BIN("텅텅 비움이", "쓰레기통 비우기 180회 완료",  null, "쓰레기통 비우기", 180, BadgeCategory.TITLE, null,"master_empty_trash.png"),

    ALARM_ALTER_START("알람 처음 변경", "알람을 처음으로 변경해보기", null, null, 1, BadgeCategory.ALARM, null, "alarm_alter.png"),

    ACCUMULATIVE_ALARM_START("알람 변경 후 집안일 1회 완료", "알람을 변경한 뒤 집안일을 1회 완료하기", null, null, 1, BadgeCategory.ACCUMULATIVE, null, "explorer.png"),
    ACCUMULATIVE_ALARM_FIVE("알람 변경 후 집안일 5회 완료", "알람을 변경한 뒤 집안일을 5회 완료하기", null, null, 5, BadgeCategory.ACCUMULATIVE, null, "explorer.png"),
    ACCUMULATIVE_ALARM_TEN("알람 변경 후 집안일 10회 완료", "알람을 변경한 뒤 집안일을 10회 완료하기", null, null, 10, BadgeCategory.ACCUMULATIVE, null, "explorer.png"),

    RECOMMEND_EXPLORER("집안일 탐색꾼", "카테고리에 있는 집안일 1회 등록하기", null, null, 1, BadgeCategory.RECOMMEND_REGISTER, null, "recommend_basic.png"),
    RECOMMEND_ADVENTURER("집안일 모험가", "카테고리에 있는 집안일 3회 등록하기", null, null, 3, BadgeCategory.RECOMMEND_REGISTER, null, "recommend_expert.png"),
    RECOMMEND_COLLECTOR("집안일 콜렉터", "카테고리에 있는 집안일 10회 등록하기", null, null, 10, BadgeCategory.RECOMMEND_REGISTER, null, "recommend_master.png"),

    BEGINNER_BEDROOM("침실 깔끔이", "침실 집안일 30회 완료", Space.BEDROOM, null, 30, BadgeCategory.SPACE,null, "beginner_bedroom.png"),
    EXPERT_BEDROOM("침실 반짝이", "침실 집안일 90회 완료", Space.BEDROOM, null, 90, BadgeCategory.SPACE, null,"expert_bedroom.png"),
    MASTER_BEDROOM("침실 번쩍이", "침실 집안일 180회 완료", Space.BEDROOM, null, 180, BadgeCategory.SPACE, null,"master_bedroom.png");


    private final String badgeName;
    private final String description;
    private final Space space;
    private final String choreTitle;
    private final int requireCount;
    private final BadgeCategory category;
    private final TimeSlot timeSlot;
    private final String imageName;

    private static final String BASE_URL = "https://homemate.io.kr/badges/";

    public String getBadgeImageUrl(){
        return BASE_URL + imageName;
    }

}
