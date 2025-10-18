package com.zerobase.homemate.recommend.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.zerobase.homemate.entity.enums.Space;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpaceChoreDto {

    @Getter
    @Setter
    public static class CreateRequest {
        @JsonProperty("space")
        private Space space;

        public CreateRequest() {}
    }
}
