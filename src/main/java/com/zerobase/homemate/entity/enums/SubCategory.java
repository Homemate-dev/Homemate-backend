package com.zerobase.homemate.entity.enums;

import lombok.Getter;

@Getter
public enum SubCategory {
    ORGANIZE_STORAGE("정리·수납"),
    CLEAN_WASH("청소·세척"),
    REPLACE_REFILL("교체·보충"),
    DISPOSE_DISCHARGE("폐기·배출");


    private final String subCategoryName;

    SubCategory(String subCategoryName) {
        this.subCategoryName = subCategoryName;
    }

}
