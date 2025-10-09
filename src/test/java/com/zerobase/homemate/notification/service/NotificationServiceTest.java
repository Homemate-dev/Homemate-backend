package com.zerobase.homemate.notification.service;

import com.zerobase.homemate.entity.*;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.notification.dto.ChoreNotificationDto;
import com.zerobase.homemate.notification.dto.NoticeDto;
import com.zerobase.homemate.notification.dto.NotificationReadDto;
import com.zerobase.homemate.repository.ChoreNotificationRepository;
import com.zerobase.homemate.repository.NoticeReadRepository;
import com.zerobase.homemate.repository.NoticeRepository;
import com.zerobase.homemate.repository.UserRepository;
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
    @Mock
    private NoticeRepository noticeRepository;
    @Mock
    private NoticeReadRepository noticeReadRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private NotificationService notificationService;

    private List<ChoreNotification> choreNotifications;
    private List<Notice> notices;
    private List<NoticeRead> noticeReads;

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

        notices = List.of(
                new Notice(1L, "공지 1", "첫번째 공지", baseDateTime.minusDays(2), null, null),
                new Notice(2L, "공지 2", "두번째 공지", baseDateTime.minusDays(1), null, null),
                new Notice(3L, "공지 3", "세번째 공지", baseDateTime, null, null)
        );

        noticeReads = List.of(
                new NoticeRead(1L, notices.get(0), user, baseDateTime.minusDays(2).plusHours(3)),
                new NoticeRead(2L, notices.get(1), user, baseDateTime.minusDays(1).plusHours(6)),
                new NoticeRead(3L, notices.get(0), user2, baseDateTime.minusDays(1))
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

        when(choreNotificationRepository.findById(notificationId)).thenReturn(choreNotifications.stream().filter(e -> notificationId.equals(e.getId())).findFirst());

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

        when(choreNotificationRepository.findById(notificationId)).thenReturn(choreNotifications.stream().filter(e -> notificationId.equals(e.getId())).findFirst());

        // when && then
        assertThatThrownBy(() -> notificationService.updateChoreNotificationToRead(userId, notificationId))
                .isInstanceOf(CustomException.class)
                .hasMessage("해당 알림을 수정할 권한이 없습니다.");
    }

    @Test
    void getNotices_Success() {
        // given
        Long userId = 1L;
        int MAX_NOTIFICATION_SIZE = 30;

        List<Notice> list = notices.stream()
                .filter(e -> e.getScheduledAt().isBefore(baseDateTime)
                )
                .sorted(Comparator.comparing(Notice::getScheduledAt).reversed())
                .limit(MAX_NOTIFICATION_SIZE)
                .toList();
        List<NoticeRead> readList = noticeReads.stream()
                .filter(e -> userId.equals(e.getUser().getId()) && list.contains(e.getNotice()))
                .toList();

        when(noticeRepository.findByScheduledAtBefore(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(list);
        when(noticeReadRepository.findByUserIdAndNoticeIdIn(userId, list.stream().map(Notice::getId).toList()))
                .thenReturn(readList);

        // when
        List<NoticeDto> result = notificationService.getNotices(userId);

        // then
        assertThat(result).hasSize(list.size());
    }

    @Test
    void updateNoticeToRead_Success_WithNoticeReadExists() {
        // given
        Long userId = 1L;
        Long notificationId = 1L;

        Notice notice = notices.stream().filter(e -> notificationId.equals(e.getId())).findFirst().get();
        when(noticeRepository.findById(notificationId)).thenReturn(Optional.of(notice));
        User user = User.builder().id(1L).build();
        when(userRepository.getReferenceById(userId)).thenReturn(user);
        NoticeRead noticeRead = noticeReads.stream().filter(e -> notice.getId().equals(e.getNotice().getId()) && user.getId().equals(e.getUser().getId())).findFirst().get();
        when(noticeReadRepository.findByUserIdAndNoticeId(user.getId(), notice.getId())).thenReturn(Optional.of(noticeRead));

        // when
        NotificationReadDto result = notificationService.updateNoticeToRead(userId, notificationId);

        // then
        assertThat(result.getId()).isEqualTo(notificationId);
        assertThat(result.getIsRead()).isTrue();
        assertThat(result.getReadAt()).isNotNull();
    }

    @Test
    void updateNoticeToRead_Success_WithNoticeReadNotExists() {
        // given
        Long userId = 1L;
        Long notificationId = 3L;

        Notice notice = notices.stream().filter(e -> notificationId.equals(e.getId())).findFirst().get();
        when(noticeRepository.findById(notificationId)).thenReturn(Optional.of(notice));
        User user = User.builder().id(1L).build();
        when(userRepository.getReferenceById(userId)).thenReturn(user);
        when(noticeReadRepository.findByUserIdAndNoticeId(user.getId(), notice.getId())).thenReturn(Optional.empty());
        NoticeRead noticeRead = new NoticeRead(4L, notice, user, baseDateTime);
        when(noticeReadRepository.save(any(NoticeRead.class))).thenReturn(noticeRead);

        // when
        NotificationReadDto result = notificationService.updateNoticeToRead(userId, notificationId);

        // then
        assertThat(result.getId()).isEqualTo(notificationId);
        assertThat(result.getIsRead()).isTrue();
        assertThat(result.getReadAt()).isNotNull();
    }

    @Test
    void updateNoticeToRead_ThrowsException_WhenChoreNotificationNotFound() {
        // given
        Long userId = 1L;
        Long notificationId = 999L;

        when(noticeRepository.findById(notificationId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> notificationService.updateNoticeToRead(userId, notificationId))
                .isInstanceOf(CustomException.class)
                .hasMessage(NOTIFICATION_NOT_FOUND.getMessage());
    }
}