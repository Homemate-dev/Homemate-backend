package com.zerobase.homemate.notification.dto;

import com.zerobase.homemate.entity.Notification;
import com.zerobase.homemate.entity.enums.NotificationCategory;
import com.zerobase.homemate.entity.enums.NotificationStatus;
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
    private Long userId;
    private Long choreId;
    private Long choreInstanceId;
    private NotificationCategory notificationCategory;
    private String title;
    private String message;
    private LocalDateTime scheduledAt;
    private NotificationStatus notificationStatus;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NotificationDto fromEntity(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .choreId(notification.getChoreId())
                .choreInstanceId(notification.getChoreInstanceId())
                .notificationCategory(notification.getNotificationCategory())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .scheduledAt(notification.getScheduledAt())
                .notificationStatus(notification.getNotificationStatus())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }
}
