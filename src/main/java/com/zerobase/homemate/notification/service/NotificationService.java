package com.zerobase.homemate.notification.service;

import com.zerobase.homemate.entity.Notification;
import com.zerobase.homemate.entity.enums.NotificationCategory;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.notification.dto.NotificationDto;
import com.zerobase.homemate.notification.dto.NotificationReadDto;
import com.zerobase.homemate.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.zerobase.homemate.exception.ErrorCode.*;

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

    @Transactional
    public NotificationReadDto updateNotificationToRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(NOTIFICATION_NOT_FOUND));

        if (!userId.equals(notification.getUserId())) {
            throw new CustomException(FORBIDDEN, "해당 알림을 수정할 권한이 없습니다.");
        }

        notification.read();

        return NotificationReadDto.from(notification);
    }
}
