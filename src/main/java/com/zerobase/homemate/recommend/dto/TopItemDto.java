package com.zerobase.homemate.recommend.dto;


import com.zerobase.homemate.entity.enums.Category;

public record TopItemDto (
        String name,
        Category category,
        Long count
){


}
