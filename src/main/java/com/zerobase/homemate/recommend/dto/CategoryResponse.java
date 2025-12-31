package com.zerobase.homemate.recommend.dto;

import com.zerobase.homemate.entity.Categories;
import com.zerobase.homemate.entity.enums.Category;

public record CategoryResponse(
        String category
) {
    public static CategoryResponse fromEntity(Category category) {
        return new CategoryResponse(
                category.getCategoryName()
        );
    }

    public static CategoryResponse fromCategories(Categories categories) {
        return new CategoryResponse(
                categories.getTitle()
        );
    }
}
