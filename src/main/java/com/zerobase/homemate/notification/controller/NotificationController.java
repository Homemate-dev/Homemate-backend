package com.zerobase.homemate.notification.controller;

import com.zerobase.homemate.entity.enums.NotificationCategory;
import com.zerobase.homemate.notification.dto.NotificationDto;
import com.zerobase.homemate.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationDto>> getNotifications(
            @RequestParam(name = "category", defaultValue = "ALL") String category
    ) {
        Long userId = 1L; // TODO: 인증에서 userId 가져오기

        if ("ALL".equalsIgnoreCase(category)) {
            List<NotificationDto> result = notificationService.getNotifications(userId);

            return ResponseEntity.ok(result);
        }

        NotificationCategory notificationCategory = NotificationCategory.from(category); // 유효하지 않은 카테고리 입력시 여기서 예외 발생
        List<NotificationDto> result = notificationService.getNotificationsByCategory(userId, notificationCategory);

        return ResponseEntity.ok(result);
    }
}
