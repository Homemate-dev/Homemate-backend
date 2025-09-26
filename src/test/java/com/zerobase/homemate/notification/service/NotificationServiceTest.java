package com.zerobase.homemate.notification.service;

import com.zerobase.homemate.entity.Notification;
import com.zerobase.homemate.entity.enums.NotificationCategory;
import com.zerobase.homemate.entity.enums.NotificationStatus;
import com.zerobase.homemate.notification.dto.NotificationDto;
import com.zerobase.homemate.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private List<Notification> notifications = new ArrayList<>();

    @BeforeEach
    void setUp() {
        LocalDateTime baseDateTime = LocalDateTime.now().withHour(12).withMinute(0).withSecond(0);

        Notification notification1 = Notification.builder()
                .id(1L)
                .userId(1L)
                .choreId(1L)
                .choreInstanceId(1L)
                .notificationCategory(NotificationCategory.CHORE)
                .title("화장실 청소")
                .scheduledAt(baseDateTime.plusDays(1))
                .notificationStatus(NotificationStatus.SCHEDULED)
                .isRead(false)
                .readAt(null)
                .build();

        Notification notification2 = Notification.builder()
                .id(2L)
                .userId(1L)
                .choreId(2L)
                .choreInstanceId(2L)
                .notificationCategory(NotificationCategory.CHORE)
                .title("쓰레기 버리기")
                .scheduledAt(baseDateTime.minusDays(1))
                .notificationStatus(NotificationStatus.SENT)
                .isRead(true)
                .readAt(baseDateTime.minusDays(1).plusHours(1))
                .build();

        notifications.addAll(List.of(notification1, notification2));
    }

    @Test
    void getNotifications_Success() {
        // given
        Long userId = 1L;

        List<Notification> list = notifications.stream().filter(e -> e.getUserId().equals(userId)).toList();
        when(notificationRepository.findAllByUserId(userId)).thenReturn(list);

        // when
        List<NotificationDto> result = notificationService.getNotifications(userId);

        // then
        assertThat(result.size()).isEqualTo(list.size());
    }

    @Test
    void getNotificationsByCategory_Success_WithCategoryChore() {
        // given
        Long userId = 1L;

        List<Notification> choreList = notifications.stream()
                .filter(e -> NotificationCategory.CHORE.equals(e.getNotificationCategory()))
                .toList();
        when(notificationRepository.findAllByUserIdAndNotificationCategory(userId, NotificationCategory.CHORE)).thenReturn(choreList);

        // when
        List<NotificationDto> result = notificationService.getNotificationsByCategory(userId, NotificationCategory.CHORE);

        // then
        assertThat(result.size()).isEqualTo(choreList.size());
    }
}