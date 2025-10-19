package com.zerobase.homemate.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChoreNotificationCreateDto {

    private Long userId;
    private Long choreInstanceId;
    private String title;
    private String message;
    private LocalDateTime scheduledAt;
}
