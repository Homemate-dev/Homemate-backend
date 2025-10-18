package com.zerobase.homemate.notification.component;

import com.zerobase.homemate.entity.ChoreInstance;
import com.zerobase.homemate.entity.enums.RepeatType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder
public class ChoreInstanceCreatedEvent {

    private Long userId;
    private Long choreInstanceId;
    private RepeatType repeatType;
    private LocalDateTime scheduledAt;

    public static ChoreInstanceCreatedEvent create(Long userId, ChoreInstance choreInstance, RepeatType repeatType) {
        LocalDate dueDate = choreInstance.getDueDate();
        LocalTime notificationTime = choreInstance.getNotificationTime();
        LocalDateTime scheduledAt = LocalDateTime.of(dueDate, notificationTime);

        return ChoreInstanceCreatedEvent.builder()
                .userId(userId)
                .choreInstanceId(choreInstance.getId())
                .repeatType(repeatType)
                .scheduledAt(scheduledAt)
                .build();
    }
}
