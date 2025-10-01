package com.zerobase.homemate.notification.service;

import com.zerobase.homemate.entity.ChoreNotification;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.notification.dto.ChoreNotificationDto;
import com.zerobase.homemate.notification.dto.NotificationReadDto;
import com.zerobase.homemate.repository.ChoreNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.zerobase.homemate.exception.ErrorCode.FORBIDDEN;
import static com.zerobase.homemate.exception.ErrorCode.NOTIFICATION_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final ChoreNotificationRepository choreNotificationRepository;

    private static final int MAX_NOTIFICATION_SIZE = 30;

    @Transactional(readOnly = true)
    public List<ChoreNotificationDto> getChoreNotifications(Long userId) {
        Pageable pageable = PageRequest.of(
                0,
                MAX_NOTIFICATION_SIZE,
                Sort.by(Sort.Order.desc("createdAt"))
        );
        List<ChoreNotification> list = choreNotificationRepository.findByUserIdAndIsCancelledFalseAndScheduledAtBefore(
                userId,
                LocalDateTime.now(),
                pageable
        );

        return list.stream().map(ChoreNotificationDto::fromEntity).toList();
    }

    @Transactional
    public NotificationReadDto updateChoreNotificationToRead(Long userId, Long notificationId) {
        ChoreNotification notification = choreNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(NOTIFICATION_NOT_FOUND));

        if (!userId.equals(notification.getUser().getId())) {
            throw new CustomException(FORBIDDEN, "해당 알림을 수정할 권한이 없습니다.");
        }

        notification.read();

        return NotificationReadDto.fromChoreNotification(notification);
    }
}
