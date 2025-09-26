package com.zerobase.homemate.notification.service;

import com.zerobase.homemate.entity.Notification;
import com.zerobase.homemate.entity.enums.NotificationCategory;
import com.zerobase.homemate.notification.dto.NotificationDto;
import com.zerobase.homemate.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public List<NotificationDto> getNotifications(Long userId) {
        List<Notification> list = notificationRepository.findAllByUserId(userId);

        return list.stream().map(NotificationDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getNotificationsByCategory(Long userId, NotificationCategory category) {
        List<Notification> list = notificationRepository.findAllByUserIdAndNotificationCategory(userId, category);

        return list.stream().map(NotificationDto::fromEntity).toList();
    }
}
