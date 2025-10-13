package com.zerobase.homemate.notification.dto;

import com.zerobase.homemate.entity.Notice;
import com.zerobase.homemate.entity.NoticeRead;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeDto {

    private Long id;
    private String title;
    private String message;
    private LocalDateTime scheduledAt;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;

    public static NoticeDto fromEntity(Notice notice, NoticeRead noticeRead) {
        return NoticeDto.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .message(notice.getMessage())
                .scheduledAt(notice.getScheduledAt())
                .isRead(noticeRead != null)
                .readAt(noticeRead != null ? noticeRead.getReadAt() : null)
                .createdAt(notice.getCreatedAt())
                .build();
    }
}
