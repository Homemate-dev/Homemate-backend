package com.zerobase.homemate.mypage.query.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zerobase.homemate.entity.enums.SocialProvider;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record MyPageResponseDto(
    Long id,
    SocialProvider provider,
    String nickname,
    String profileImageUrl,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime lastLoginAt,
    Boolean masterEnabled,
    Boolean choreEnabled,
    Boolean noticeEnabled,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    LocalTime notificationTime,
    Integer totalBadgeCount,
    Integer acquiredBadgeCount
) {
    public static MyPageResponseDto of(MyPageDto myPageDto, int totalBadgeCount, int acquiredBadgeCount) {
        return new MyPageResponseDto(
                myPageDto.id(),
                myPageDto.provider(),
                myPageDto.nickname(),
                myPageDto.profileImageUrl(),
                myPageDto.createdAt(),
                myPageDto.updatedAt(),
                myPageDto.lastLoginAt(),
                myPageDto.masterEnabled(),
                myPageDto.choreEnabled(),
                myPageDto.noticeEnabled(),
                myPageDto.notificationTime(),
                totalBadgeCount,
                acquiredBadgeCount
        );
    }
}
