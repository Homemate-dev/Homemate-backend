package com.zerobase.homemate.notification.component;

import com.zerobase.homemate.entity.Notice;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NoticeCreatedEvent {

    private String title;
    private String message;

    public static NoticeCreatedEvent create(Notice notice) {
        return NoticeCreatedEvent.builder()
                .title(notice.getTitle())
                .message(notice.getMessage())
                .build();
    }
}
