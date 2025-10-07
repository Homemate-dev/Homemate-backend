package com.zerobase.homemate.notification.dto;

import com.zerobase.homemate.entity.Notice;
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
    // TODO: NoticeRead 추가 후 필드 추가 예정
    // private Boolean isRead;
    // private LocalDateTime readAt;
    private LocalDateTime createdAt;

    public static NoticeDto fromEntity(Notice notice) {
        return NoticeDto.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .message(notice.getMessage())
                .scheduledAt(notice.getScheduledAt())
                .createdAt(notice.getCreatedAt())
                .build();
    }
}
