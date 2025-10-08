package com.zerobase.homemate.notification.service;

import com.zerobase.homemate.entity.ChoreNotification;
import com.zerobase.homemate.entity.Notice;
import com.zerobase.homemate.entity.NoticeRead;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.notification.dto.ChoreNotificationDto;
import com.zerobase.homemate.notification.dto.NoticeDto;
import com.zerobase.homemate.notification.dto.NotificationReadDto;
import com.zerobase.homemate.repository.ChoreNotificationRepository;
import com.zerobase.homemate.repository.NoticeReadRepository;
import com.zerobase.homemate.repository.NoticeRepository;
import com.zerobase.homemate.repository.UserRepository;
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

    private static final int MAX_NOTIFICATION_SIZE = 30;
    private final UserRepository userRepository;
    private final ChoreNotificationRepository choreNotificationRepository;
    private final NoticeRepository noticeRepository;
    private final NoticeReadRepository noticeReadRepository;

    @Transactional(readOnly = true)
    public List<ChoreNotificationDto> getChoreNotifications(Long userId) {
        Pageable pageable = PageRequest.of(
                0,
                MAX_NOTIFICATION_SIZE,
                Sort.by(Sort.Order.desc("scheduledAt"))
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

    @Transactional(readOnly = true)
    public List<NoticeDto> getNotices(Long userId) { // userId는 NoticeRead 추가 후 사용 예정
        Pageable pageable = PageRequest.of(
                0,
                MAX_NOTIFICATION_SIZE,
                Sort.by(Sort.Order.desc("scheduledAt"))
        );

        List<Notice> list = noticeRepository.findByScheduledAtBefore(LocalDateTime.now(), pageable);

        return list.stream().map(NoticeDto::fromEntity).toList();
    }

    @Transactional
    public NotificationReadDto updateNoticeToRead(Long userId, Long notificationId) {
        Notice notice = noticeRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(NOTIFICATION_NOT_FOUND));

        User user = userRepository.getReferenceById(userId);

        NoticeRead noticeRead = noticeReadRepository.findByNoticeAndUser(notice, user).orElseGet(
                () -> noticeReadRepository.save(NoticeRead.builder()
                        .notice(notice)
                        .user(user)
                        .build())
        );

        return NotificationReadDto.fromNotice(notice, noticeRead);
    }
}
