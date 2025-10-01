package com.zerobase.homemate.notification.service;

import com.zerobase.homemate.entity.ChoreInstance;
import com.zerobase.homemate.entity.ChoreNotification;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.notification.dto.ChoreNotificationDto;
import com.zerobase.homemate.notification.dto.NotificationReadDto;
import com.zerobase.homemate.repository.ChoreNotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.zerobase.homemate.exception.ErrorCode.NOTIFICATION_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private final LocalDateTime baseDateTime = LocalDateTime.now().withHour(12).withMinute(0).withSecond(0).withNano(0);
    @Mock
    private ChoreNotificationRepository choreNotificationRepository;
    @InjectMocks
    private NotificationService notificationService;
    private List<ChoreNotification> choreNotifications;

    private static ChoreInstance buildChoreInstance(long id) {
        return ChoreInstance.builder().id(id).build();
    }

    @BeforeEach
    void setUp() {
        // 테스트 데이터 생성
        User user = User.builder().id(1L).build();
        User user2 = User.builder().id(2L).build();


        choreNotifications = List.of(
                new ChoreNotification(1L, user, buildChoreInstance(1L), "화장실 청소", null, baseDateTime.minusDays(3), false, true, baseDateTime.minusDays(1).plusHours(1), null, null),
                new ChoreNotification(2L, user, buildChoreInstance(2L), "재활용 쓰레기 버리기", null, baseDateTime.minusDays(2), false, true, baseDateTime.minusDays(1).plusHours(1), null, null),
                new ChoreNotification(3L, user, buildChoreInstance(3L), "방 청소", null, baseDateTime.minusDays(1), true, false, null, null, null),
                new ChoreNotification(4L, user, buildChoreInstance(4L), "재활용 쓰레기 버리기", null, baseDateTime.minusHours(1), false, false, null, null, null),
                new ChoreNotification(5L, user2, buildChoreInstance(5L), "방 청소", null, baseDateTime.minusHours(1), false, false, null, null, null)
        );
    }

    @Test
    void getChoreNotifications_Success() {
        // given
        Long userId = 1L;
        int MAX_NOTIFICATION_SIZE = 30;

        List<ChoreNotification> list = choreNotifications.stream()
                .filter(e -> e.getUser().getId().equals(userId)
                        && e.getIsCancelled().equals(false)
                        && e.getScheduledAt().isBefore(baseDateTime)
                )
                .sorted(Comparator.comparing(ChoreNotification::getScheduledAt).reversed())
                .limit(MAX_NOTIFICATION_SIZE)
                .toList();

        when(choreNotificationRepository.findByUserIdAndIsCancelledFalseAndScheduledAtBefore(eq(userId), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(list);

        // when
        List<ChoreNotificationDto> result = notificationService.getChoreNotifications(userId);

        // then
        assertThat(result).hasSize(list.size());
    }

    @Test
    void updateChoreNotificationToRead_Success() {
        // given
        Long userId = 1L;
        Long notificationId = 1L;

        ChoreNotification notification = choreNotifications.stream().filter(e -> notificationId.equals(e.getId())).findFirst().get();
        when(choreNotificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        // when
        NotificationReadDto result = notificationService.updateChoreNotificationToRead(userId, notificationId);

        // then
        assertThat(result.getId()).isEqualTo(notificationId);
        assertThat(result.getIsRead()).isTrue();
        assertThat(result.getReadAt()).isNotNull();
    }

    @Test
    void updateNotificationToRead_ThrowsException_WhenChoreNotificationNotFound() {
        // given
        Long userId = 1L;
        Long notificationId = 999L;

        when(choreNotificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        // when && then
        assertThatThrownBy(() -> notificationService.updateChoreNotificationToRead(userId, notificationId))
                .isInstanceOf(CustomException.class)
                .hasMessage(NOTIFICATION_NOT_FOUND.getMessage());
    }

    @Test
    void updateChoreNotificationToRead_ThrowsException_WhenUserIdNotMatched() {
        // given
        Long userId = 999L;
        Long notificationId = 1L;

        ChoreNotification notification = choreNotifications.stream().filter(e -> notificationId.equals(e.getId())).findFirst().get();
        when(choreNotificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        // when && then
        assertThatThrownBy(() -> notificationService.updateChoreNotificationToRead(userId, notificationId))
                .isInstanceOf(CustomException.class)
                .hasMessage("해당 알림을 수정할 권한이 없습니다.");
    }
}