package com.zerobase.homemate.badge;

import com.zerobase.homemate.badge.service.BadgeService;
import com.zerobase.homemate.badge.service.UserBadgeStatsService;
import com.zerobase.homemate.entity.Badge;
import com.zerobase.homemate.entity.ChoreInstance;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.enums.BadgeType;
import com.zerobase.homemate.entity.enums.TimeSlot;
import com.zerobase.homemate.entity.enums.UserRole;
import com.zerobase.homemate.entity.enums.UserStatus;
import com.zerobase.homemate.notification.controller.NotionWebhookController;
import com.zerobase.homemate.repository.BadgeRepository;
import com.zerobase.homemate.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest
public class BadgeServiceTimeSlotTest {


    @Autowired
    BadgeService badgeService;

    @MockitoBean
    UserBadgeStatsService userBadgeStatsService;

    @MockitoBean
    BadgeRepository badgeRepository;

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    NotionWebhookController notionWebhookController;

    @MockitoBean
    StringRedisTemplate stringRedisTemplate;

    @Test
    void timeSlot_should_be_BEFORE_10() {
        User user = User.builder()
                .id(3L)
                .userRole(UserRole.USER)
                .profileName("testUser")
                .userStatus(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        ChoreInstance chore = mock(ChoreInstance.class);
        LocalDateTime time = LocalDateTime.of(2026, 1, 10, 9, 59);
        given(chore.getCompletedAt()).willReturn(time);

        given(badgeRepository.existsByUserAndBadgeType(any(), any()))
                .willReturn(true); // 뱃지 저장 로직 스킵

        // when
        badgeService.evaluateBadgesOnCompletion(user, chore);

        // then
        verify(userBadgeStatsService)
                .incrementTimeCount(3L, TimeSlot.BEFORE_10);
    }

    @Test
    void after_6pm_should_be_AFTER_6() {
        // given
        User user = User.builder()
                .id(5L)
                .userRole(UserRole.USER)
                .profileName("testUser2")
                .userStatus(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(user);

        ChoreInstance chore = mock(ChoreInstance.class);
        LocalDateTime time = LocalDateTime.of(2026, 1, 7, 20, 59);
        given(chore.getCompletedAt()).willReturn(time);

        given(badgeRepository.existsByUserAndBadgeType(any(), any()))
                .willReturn(true);

        // when
        badgeService.evaluateBadgesOnCompletion(user, chore);

        // then
        verify(userBadgeStatsService)
                .incrementTimeCount(5L, TimeSlot.AFTER_6PM);
    }

    @Test
    void BEFORE_10_progress_ten_times_getBadge() {
        // given
        User user = User.builder()
                .id(6L)
                .userRole(UserRole.USER)
                .profileName("testUser3")
                .userStatus(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        ChoreInstance chore = mock(ChoreInstance.class);
        given(chore.getCompletedAt())
                .willReturn(LocalDateTime.of(2026, 1, 9, 9, 0));

        // increment 이후 조회 시 9회로 가정
        given(userBadgeStatsService.getTimeCount(6L, TimeSlot.BEFORE_10))
                .willReturn(10L);

        // BadgeType 순회 시 MIRACLE_MORNING만 false, 나머지는 true
        given(badgeRepository.existsByUserAndBadgeType(eq(user), any()))
                .willAnswer(invocation -> {
                    BadgeType type = invocation.getArgument(1);
                    return type != BadgeType.MIRACLE_MORNING;
                });

        // when
        badgeService.evaluateBadgesOnCompletion(user, chore);

        // then
        verify(badgeRepository).saveAll(
                argThat((List<Badge> badges) ->
                        badges.stream()
                                .anyMatch(b -> b.getBadgeType() == BadgeType.MIRACLE_MORNING)
                )
        );
    }
}
