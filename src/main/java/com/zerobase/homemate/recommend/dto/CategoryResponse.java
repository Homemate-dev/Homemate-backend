package com.zerobase.homemate.recommend.dto;

import com.zerobase.homemate.entity.Category;

public record CategoryResponse(
        String name
) {
    public static CategoryResponse fromEntity(Category category) {
        return new CategoryResponse(
                category.getNameKo()
        );
    }
}
