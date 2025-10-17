package com.zerobase.homemate.mission.dto;

import com.zerobase.homemate.entity.Mission;
import com.zerobase.homemate.entity.UserMission;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class MissionDto {

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class Response {

        private Long id;
        private String title;
        private Integer targetCount;
        private Integer currentCount;
        private boolean isCompleted;

        public static Response of(Mission mission,
            @Nullable UserMission userMission) {
            int currentCount =
                (userMission != null) ? userMission.getCurrentCount() : 0;
            boolean isCompleted =
                (userMission != null) ? userMission.getIsCompleted() : false;

            return Response.builder()
                .id(mission.getId())
                .title(mission.getTitle())
                .targetCount(mission.getTargetCount())
                .currentCount(currentCount)
                .isCompleted(isCompleted)
                .build();
        }
    }
}
