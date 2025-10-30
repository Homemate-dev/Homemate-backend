package com.zerobase.homemate.mypage.query.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zerobase.homemate.entity.enums.SocialProvider;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record MyPageResponseDto(
    Long id,
    SocialProvider provider,
    String nickname,
    String profileImgUrl,
    LocalDateTime createdAt,
    LocalDateTime lastLoginAt,
    Boolean masterEnabled,
    Boolean choreEnabled,
    Boolean noticeEnabled,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    LocalTime notificationTime,
    LocalDateTime updatedAt,
    Integer totalBadgeCount,
    Integer acquiredBadgeCount
) {}
