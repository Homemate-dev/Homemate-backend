package com.zerobase.homemate.notification.controller;

import com.zerobase.homemate.auth.security.UserPrincipal;
import com.zerobase.homemate.entity.enums.NotificationCategory;
import com.zerobase.homemate.notification.dto.NotificationDto;
import com.zerobase.homemate.notification.dto.NotificationReadDto;
import com.zerobase.homemate.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationDto>> getNotifications(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(name = "category", defaultValue = "ALL") String category
    ) {
        Long userId = userPrincipal.id();

        if ("ALL".equalsIgnoreCase(category)) {
            List<NotificationDto> result = notificationService.getNotifications(userId);

            return ResponseEntity.ok(result);
        }

        NotificationCategory notificationCategory = NotificationCategory.from(category); // 유효하지 않은 카테고리 입력시 여기서 예외 발생
        List<NotificationDto> result = notificationService.getNotificationsByCategory(userId, notificationCategory);

        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{notificationId}")
    public ResponseEntity<NotificationReadDto> readNotification(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long notificationId
    ) {
        Long userId = userPrincipal.id();

        NotificationReadDto result = notificationService.updateNotificationToRead(userId, notificationId);

        return ResponseEntity.ok(result);
    }
}
