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
    boolean masterEnabled,
    boolean choreEnabled,
    boolean noticeEnabled,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    LocalTime notificationTime,
    LocalDateTime updatedAt

    // TODO: 뱃지 엔티티 추가 시, 사용자 획득 뱃지 수 필드 추가
) {}
