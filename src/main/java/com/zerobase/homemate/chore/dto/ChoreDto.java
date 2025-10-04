package com.zerobase.homemate.chore.dto;

import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.SpaceChore;
import com.zerobase.homemate.entity.enums.RepeatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ChoreDto {

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class CreateRequest {
        @NotBlank(message = "집안일 제목은 필수입니다")
        private String title;

        @NotNull(message = "알림 여부는 필수입니다")
        private Boolean notificationYn;

        private LocalTime notificationTime;

        @NotNull(message = "공간 입력은 필수입니다.")
        private SpaceChore spaceChore;


        @NotNull(message = "반복 타입은 필수입니다")
        private RepeatType repeatType;

        private Integer repeatInterval;

        @NotNull(message = "시작 일자는 필수입니다")
        private LocalDate startDate;

        @NotNull(message = "종료 일자는 필수입니다")
        private LocalDate endDate;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class Response {
        private Long id;
        private String title;
        private Boolean notificationYn;
        private LocalTime notificationTime;
        private SpaceChore spaceChore;
        private RepeatType repeatType;
        private Integer repeatInterval;
        private LocalDate startDate;
        private LocalDate endDate;
        private Boolean isDeleted;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime deletedAt;

        public static Response fromEntity(Chore chore) {
            return Response.builder()
                .id(chore.getId())
                .title(chore.getTitle())
                .notificationYn(chore.getNotificationYn())
                .notificationTime(chore.getNotificationTime())
                .spaceChore(chore.getSpaceChore())
                .repeatType(chore.getRepeatType())
                .repeatInterval(chore.getRepeatInterval())
                .startDate(chore.getStartDate())
                .endDate(chore.getEndDate())
                .isDeleted(chore.getIsDeleted())
                .createdAt(chore.getCreatedAt())
                .updatedAt(chore.getUpdatedAt())
                .deletedAt(chore.getDeletedAt())
                .build();
        }
    }
}
