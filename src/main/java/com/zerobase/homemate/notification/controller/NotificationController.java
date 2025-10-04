package com.zerobase.homemate.notification.controller;

import com.zerobase.homemate.auth.security.UserPrincipal;
import com.zerobase.homemate.notification.dto.ChoreNotificationDto;
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

    @GetMapping("/chores")
    public ResponseEntity<List<ChoreNotificationDto>> getChoreNotifications(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal.id();

        List<ChoreNotificationDto> result = notificationService.getChoreNotifications(userId);

        return ResponseEntity.ok(result);
    }

    @PatchMapping("/chores/{notificationId}")
    public ResponseEntity<NotificationReadDto> readChoreNotification(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long notificationId
    ) {
        Long userId = userPrincipal.id();

        NotificationReadDto result = notificationService.updateChoreNotificationToRead(userId, notificationId);

        return ResponseEntity.ok(result);
    }
}
