package com.zerobase.homemate.entity.enums;


import lombok.Getter;

@Getter
public enum Space {
    KITCHEN("주방"),
    LIVING_ROOM("거실"),
    BEDROOM("침실"),
    PORCH("현관"),
    ETC("기타");

    private final String description;

    Space(String description) {
        this.description = description;
    }
}

