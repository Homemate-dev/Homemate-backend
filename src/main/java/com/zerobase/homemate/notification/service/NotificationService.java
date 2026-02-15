package com.zerobase.homemate.notification.service;

import com.zerobase.homemate.entity.*;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.notification.component.NoticeCreatedEvent;
import com.zerobase.homemate.notification.dto.*;
import com.zerobase.homemate.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.zerobase.homemate.exception.ErrorCode.FORBIDDEN;
import static com.zerobase.homemate.exception.ErrorCode.NOTIFICATION_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final int MAX_NOTIFICATION_SIZE = 30;
    private final UserRepository userRepository;
    private final ChoreInstanceRepository choreInstanceRepository;
    private final ChoreNotificationRepository choreNotificationRepository;
    private final NoticeRepository noticeRepository;
    private final NoticeReadRepository noticeReadRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<ChoreNotificationDto> getChoreNotifications(Long userId) {
        Pageable pageable = PageRequest.of(
                0,
                MAX_NOTIFICATION_SIZE,
                Sort.by(Sort.Order.desc("scheduledAt"))
        );

        List<ChoreNotification> list = choreNotificationRepository.findChoreNotifications(
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
    public List<NoticeDto> getNotices(Long userId) {
        Pageable pageable = PageRequest.of(
                0,
                MAX_NOTIFICATION_SIZE,
                Sort.by(Sort.Order.desc("scheduledAt"))
        );

        List<Notice> notices = noticeRepository.findByScheduledAtBefore(LocalDateTime.now(), pageable);

        // 빈 리스트인 경우 조기 반환
        if (notices.isEmpty()) {
            return List.of();
        }

        // 각 Notice에 해당하는 NoticeRead가 있는지 찾아서 매핑
        User user = userRepository.getReferenceById(userId);
        List<NoticeRead> noticeReads = noticeReadRepository.findByUserAndNoticeIn(user, notices);
        Map<Long, NoticeRead> noticeReadMap = noticeReads.stream()
                .collect(Collectors.toMap(e -> e.getNotice().getId(), Function.identity()));

        return notices.stream()
                .map(e -> NoticeDto.fromEntity(e, noticeReadMap.get(e.getId())))
                .toList();
    }

    @Transactional
    public NotificationReadDto markNoticeAsRead(Long userId, Long notificationId) {
        Notice notice = noticeRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(NOTIFICATION_NOT_FOUND));

        User user = userRepository.getReferenceById(userId);

        NoticeRead noticeRead = noticeReadRepository.findByUserAndNotice(user, notice).orElseGet(
                () -> noticeReadRepository.save(NoticeRead.builder()
                        .notice(notice)
                        .user(user)
                        .build())
        );

        return NotificationReadDto.fromNotice(notice, noticeRead);
    }

    @Transactional
    public NotificationReadDto createChoreNotification(ChoreNotificationCreateDto request) {
        User user = userRepository.getReferenceById(request.getUserId());
        ChoreInstance choreInstance = choreInstanceRepository.getReferenceById(request.getChoreInstanceId());

        ChoreNotification notification = ChoreNotification.builder()
                .user(user)
                .choreInstance(choreInstance)
                .title(request.getTitle())
                .message(request.getMessage())
                .scheduledAt(request.getScheduledAt())
                .isCancelled(false)
                .isRead(false)
                .readAt(null)
                .build();

        ChoreNotification saved = choreNotificationRepository.save(notification);

        return NotificationReadDto.fromChoreNotification(saved);
    }

    @Transactional
    public NoticeDto createNotice(NoticeCreateDto request) {
        Notice notice = Notice.builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .url(request.getUrl())
                .scheduledAt(LocalDateTime.now())
                .build();

        Notice saved = noticeRepository.save(notice);

        eventPublisher.publishEvent(NoticeCreatedEvent.create(saved));

        return NoticeDto.fromEntity(saved, null);
    }
}
