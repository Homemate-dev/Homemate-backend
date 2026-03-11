package com.zerobase.homemate.notification.push.dto;

import com.zerobase.homemate.entity.FcmToken;
import com.zerobase.homemate.entity.enums.DeviceType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class FcmTokenDto {

    @Getter
    @NoArgsConstructor
    public static class Request {

        @NotBlank
        private String token;
        private DeviceType deviceType;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {

        private Long id;
        private Long userId;
        private String token;
        private DeviceType deviceType;
        private Boolean isActive;
        private LocalDateTime lastUsedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response fromEntity(FcmToken fcmToken) {
            return Response.builder()
                    .id(fcmToken.getId())
                    .userId(fcmToken.getUser().getId())
                    .token(fcmToken.getToken())
                    .deviceType(fcmToken.getDeviceType())
                    .isActive(fcmToken.getIsActive())
                    .lastUsedAt(fcmToken.getLastUsedAt())
                    .createdAt(fcmToken.getCreatedAt())
                    .updatedAt(fcmToken.getUpdatedAt())
                    .build();
        }
    }
}
