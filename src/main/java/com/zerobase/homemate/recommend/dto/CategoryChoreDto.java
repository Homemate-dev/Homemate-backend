package com.zerobase.homemate.recommend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zerobase.homemate.entity.enums.Category;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryChoreDto {

    @Getter
    @Setter
    public static class CreateRequest {
        @JsonProperty("category")
        private Category category;

        // 기본 생성자 필수
        public CreateRequest() {}
    }
}
