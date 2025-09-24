package com.zerobase.homemate.chores.dto;

import com.zerobase.homemate.entity.Chores;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ChoresDto {

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

        private String notificationTime;

        private String space;

        @NotNull(message = "반복 타입은 필수입니다")
        private Chores.RepeatType repeatType;

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
        private String notificationTime;
        private String space;
        private Chores.RepeatType repeatType;
        private Integer repeatInterval;
        private LocalDate startDate;
        private LocalDate endDate;
        private Boolean isDeleted;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime deletedAt;

        public static Response fromEntity(Chores chores) {
            return Response.builder()
                .id(chores.getId())
                .title(chores.getTitle())
                .notificationYn(chores.getNotificationYn())
                .notificationTime(String.valueOf(chores.getNotificationTime()))
                .space(chores.getSpace())
                .repeatType(chores.getRepeatType())
                .repeatInterval(chores.getRepeatInterval())
                .startDate(chores.getStartDate())
                .endDate(chores.getEndDate())
                .isDeleted(chores.getIsDeleted())
                .createdAt(chores.getCreatedAt())
                .updatedAt(chores.getUpdatedAt())
                .deletedAt(chores.getDeletedAt())
                .build();
        }
    }
}
