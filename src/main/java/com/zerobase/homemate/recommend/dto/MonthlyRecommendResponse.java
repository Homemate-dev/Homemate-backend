package com.zerobase.homemate.recommend.dto;

import com.zerobase.homemate.entity.Categories;

public record MonthlyRecommendResponse (
        Long categoriesId,
        String categoryName
){

    public static MonthlyRecommendResponse from(Categories categories) {
        return new MonthlyRecommendResponse(
                categories.getId(),
                categories.getTitle()
        );
    }
}
