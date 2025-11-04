package com.zerobase.homemate.recommend.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.zerobase.homemate.entity.SpaceChore;
import com.zerobase.homemate.entity.enums.RepeatType;
import com.zerobase.homemate.entity.enums.Space;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class Response {
        private Long id;
        private String code;
        private String titleKo;
        private RepeatType repeatType;
        private Integer repeatInterval;
        private Space space;

        public static SpaceChoreDto.Response fromEntity(SpaceChore spaceChore) {
            return SpaceChoreDto.Response.builder()
                .id(spaceChore.getId())
                .code(spaceChore.getCode())
                .titleKo(spaceChore.getTitleKo())
                .repeatType(spaceChore.getRepeatType())
                .repeatInterval(spaceChore.getRepeatInterval())
                .space(spaceChore.getSpace())
                .build();
        }
    }
}
