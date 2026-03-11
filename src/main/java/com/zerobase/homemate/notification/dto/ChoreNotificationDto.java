package com.zerobase.homemate.notification.dto;

import com.zerobase.homemate.entity.ChoreNotification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChoreNotificationDto {

    private Long id;
    private Long choreInstanceId;
    private String title;
    private String message;
    private LocalDateTime scheduledAt;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;

    public static ChoreNotificationDto fromEntity(ChoreNotification notification) {
        return ChoreNotificationDto.builder()
                .id(notification.getId())
                .choreInstanceId(notification.getChoreInstance().getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .scheduledAt(notification.getScheduledAt())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
