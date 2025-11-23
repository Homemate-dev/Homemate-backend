package com.zerobase.homemate.chore.dto;

import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.enums.RegistrationType;
import com.zerobase.homemate.entity.enums.RepeatType;
import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.mission.dto.MissionDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.experimental.SuperBuilder;

public class ChoreDto {

    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    @Getter
    @Setter
    public abstract static class Request {
        @NotBlank(message = "집안일 제목은 필수입니다")
        private String title;

        @NotNull(message = "알림 여부는 필수입니다")
        private Boolean notificationYn;

        private LocalTime notificationTime;

        @NotNull(message = "공간 입력은 필수입니다.")
        private Space space;

        @NotNull(message = "반복 타입은 필수입니다")
        private RepeatType repeatType;

        private Integer repeatInterval;

        @NotNull(message = "시작 일자는 필수입니다")
        private LocalDate startDate;

        @NotNull(message = "종료 일자는 필수입니다")
        private LocalDate endDate;

        @NotNull(message = "등록 경로는 필수입니다")
        private Boolean recommendYn;
    }

    @NoArgsConstructor
    @SuperBuilder
    @Getter
    @Setter
    public static class CreateRequest extends Request { }

    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    @Getter
    @Setter
    public static class UpdateRequest extends Request {
        private Boolean applyToAfter;
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
        private RepeatType repeatType;
        private Integer repeatInterval;
        private LocalDate startDate;
        private LocalDate endDate;
        private Boolean isDeleted;
        private Space space;
        private RegistrationType registrationType;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime deletedAt;

        public static Response fromEntity(Chore chore) {
            return Response.builder()
                .id(chore.getId())
                .title(chore.getTitle())
                .notificationYn(chore.getNotificationYn())
                .notificationTime(chore.getNotificationTime())
                .repeatType(chore.getRepeatType())
                .space(chore.getSpace())
                .repeatInterval(chore.getRepeatInterval())
                .startDate(chore.getStartDate())
                .endDate(chore.getEndDate())
                .isDeleted(chore.getIsDeleted())
                .registrationType(chore.getRegistrationType())
                .createdAt(chore.getCreatedAt())
                .updatedAt(chore.getUpdatedAt())
                .deletedAt(chore.getDeletedAt())
                .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ApiResponse<T> {
        private T data;

        @Builder.Default
        private List<MissionDto.Response> missionResults = List.of();
    }
}
