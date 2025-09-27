package com.zerobase.homemate.notification.service;

import com.zerobase.homemate.entity.Notification;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.notification.dto.NotificationDto;
import com.zerobase.homemate.notification.dto.NotificationReadDto;
import com.zerobase.homemate.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.zerobase.homemate.entity.enums.NotificationCategory.CHORE;
import static com.zerobase.homemate.entity.enums.NotificationCategory.NOTICE;
import static com.zerobase.homemate.entity.enums.NotificationStatus.SCHEDULED;
import static com.zerobase.homemate.entity.enums.NotificationStatus.SENT;
import static com.zerobase.homemate.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private List<Notification> notifications;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 생성
        LocalDateTime baseDateTime = LocalDateTime.now().withHour(12).withMinute(0).withSecond(0);

        notifications = List.of(
                new Notification(1L, 1L, 1L, 1L, CHORE, "화장실 청소", "", baseDateTime.plusDays(1), SCHEDULED, false, null, null, null),
                new Notification(2L, 1L, 2L, 2L, CHORE, "쓰레기 버리기", "", baseDateTime.minusDays(1), SENT, true, baseDateTime.minusDays(1).plusHours(6), null, null),
                new Notification(3L, null, null, null, NOTICE, "공지 사항", "", baseDateTime, SENT, false, null, null, null),
                new Notification(4L, 1L, 2L, 3L, CHORE, "쓰레기 버리기", "", baseDateTime.plusDays(3), SCHEDULED, false, null, null, null),
                new Notification(5L, 2L, 3L, 4L, CHORE, "창문 닦기", "", baseDateTime.plusDays(3), SCHEDULED, false, null, null, null)
        );
    }

    @Test
    void getNotifications_Success() {
        // given
        Long userId = 1L;

        List<Notification> list = notifications.stream().filter(e -> userId.equals(e.getUserId())).toList();
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
                .filter(e -> CHORE.equals(e.getNotificationCategory()))
                .toList();
        when(notificationRepository.findAllByUserIdAndNotificationCategory(userId, CHORE)).thenReturn(choreList);

        // when
        List<NotificationDto> result = notificationService.getNotificationsByCategory(userId, CHORE);

        // then
        assertThat(result.size()).isEqualTo(choreList.size());
    }

    @Test
    void updateNotificationToRead_Success() {
        // given
        Long userId = 1L;
        Long notificationId = 1L;

        Notification notification = notifications.stream().filter(e -> notificationId.equals(e.getId())).findFirst().get();
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        // when
        NotificationReadDto result = notificationService.updateNotificationToRead(userId, notificationId);

        // then
        assertThat(result.getId()).isEqualTo(notificationId);
        assertThat(result.getIsRead()).isTrue();
        assertThat(result.getReadAt()).isNotNull();
    }

    @Test
    void updateNotificationToRead_ThrowsException_WhenNotificationNotFound() {
        // given
        Long userId = 1L;
        Long notificationId = 999L;

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        // when && then
        assertThatThrownBy(() -> notificationService.updateNotificationToRead(userId, notificationId))
                .isInstanceOf(CustomException.class)
                .hasMessage(NOTIFICATION_NOT_FOUND.getMessage());
    }

    @Test
    void updateNotificationToRead_ThrowsException_WhenUserIdNotMatched() {
        // given
        Long userId = 999L;
        Long notificationId = 1L;

        Notification notification = notifications.stream().filter(e -> notificationId.equals(e.getId())).findFirst().get();
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        // when && then
        assertThatThrownBy(() -> notificationService.updateNotificationToRead(userId, notificationId))
                .isInstanceOf(CustomException.class)
                .hasMessage("해당 알림을 수정할 권한이 없습니다.");
    }
}