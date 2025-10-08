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
public class NotificationReadDto {

    private Long id;
    private Boolean isRead;
    private LocalDateTime readAt;

    public static NotificationReadDto fromChoreNotification(ChoreNotification notification) {
        return NotificationReadDto.builder()
                .id(notification.getId())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .build();
    }
}
