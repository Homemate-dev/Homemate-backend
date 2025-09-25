package com.zerobase.homemate.notification.dto;

import com.zerobase.homemate.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {

    private Long id;
    private Long choreId;
    private Long choreInstanceId;
    private Notification.Category category;
    private String title;
    private String message;
    private LocalDateTime scheduledAt;
    private Notification.Status status;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NotificationDto fromEntity(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .choreId(notification.getChoreId())
                .choreInstanceId(notification.getChoreInstanceId())
                .category(notification.getCategory())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .scheduledAt(notification.getScheduledAt())
                .status(notification.getStatus())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }
}
