package com.zerobase.homemate.recommend.dto;

import com.zerobase.homemate.entity.enums.SubCategory;

public record SubCategoryResponse (
        String code,
        String subCategoryName
){
    public static SubCategoryResponse fromSub(SubCategory subCategory){
        return new SubCategoryResponse(
                subCategory.name(),
                subCategory.getSubCategoryName()
        );
    }
}
