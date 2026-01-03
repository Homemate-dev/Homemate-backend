package com.zerobase.homemate.entity.enums;

import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Space {
    KITCHEN("주방"),
    BATHROOM("욕실"),
    BEDROOM("침실"),
    PORCH("현관"),
    ETC("공간-기타");

    private final String spaceName;

    Space(String spaceName) {
        this.spaceName = spaceName;
    }

    public static Space from (String value) {
        return Arrays.stream(values())
                .filter(v -> v.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.URI_NOT_FOUND));
    }
}

