package com.zerobase.homemate.recommend.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.zerobase.homemate.entity.SpaceChore;
import com.zerobase.homemate.entity.UserNotificationSetting;
import com.zerobase.homemate.entity.enums.RepeatType;
import com.zerobase.homemate.entity.enums.Space;
import java.time.LocalDate;
import java.time.LocalTime;
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
        private LocalDate startDate;
        private LocalDate endDate;
        private boolean choreEnabled;
        private LocalTime notificationTime;

        public static SpaceChoreDto.Response of(SpaceChore spaceChore,
            UserNotificationSetting userNotificationSetting,
            LocalDate endDate) {
            return Response.builder()
                .id(spaceChore.getId())
                .code(spaceChore.getCode())
                .titleKo(spaceChore.getTitleKo())
                .repeatType(spaceChore.getRepeatType())
                .repeatInterval(spaceChore.getRepeatInterval())
                .space(spaceChore.getSpace())
                .startDate(LocalDate.now())
                .endDate(endDate)
                .choreEnabled(userNotificationSetting.isChoreEnabled())
                .notificationTime(userNotificationSetting.getNotificationTime())
                .build();
        }
    }
}
