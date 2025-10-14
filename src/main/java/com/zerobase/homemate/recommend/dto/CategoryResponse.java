package com.zerobase.homemate.recommend.dto;

import com.zerobase.homemate.entity.enums.Category;

public record CategoryResponse(
        String category
) {
    public static CategoryResponse fromEntity(Category category) {
        return new CategoryResponse(
                category.getCategoryName()
        );
    }
}
