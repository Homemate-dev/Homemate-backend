package com.zerobase.homemate.chore.dto;

import com.zerobase.homemate.entity.ChoreInstance;
import com.zerobase.homemate.entity.enums.ChoreStatus;
import com.zerobase.homemate.entity.enums.RepeatType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ChoreInstanceDto {

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Getter
    public static class Response {
        private Long id;
        private Long choreId;
        private String titleSnapshot;
        private LocalDate dueDate;
        private LocalTime notificationTime;
        private ChoreStatus choreStatus;
        private RepeatType repeatType;
        private Integer repeatInterval;
        private LocalDateTime completedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static ChoreInstanceDto.Response fromEntity(ChoreInstance choreInstance) {
            return Response.builder()
                .id(choreInstance.getId())
                .choreId(choreInstance.getChore().getId())
                .titleSnapshot(choreInstance.getTitleSnapshot())
                .dueDate(choreInstance.getDueDate())
                .notificationTime(choreInstance.getNotificationTime())
                .choreStatus(choreInstance.getChoreStatus())
                .repeatType(choreInstance.getChore().getRepeatType())
                .repeatInterval(choreInstance.getChore().getRepeatInterval())
                .completedAt(choreInstance.getCompletedAt())
                .createdAt(choreInstance.getCreatedAt())
                .updatedAt(choreInstance.getUpdatedAt())
                .build();
        }
    }
}
