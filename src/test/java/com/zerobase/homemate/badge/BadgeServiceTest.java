package com.zerobase.homemate.badge;

import com.zerobase.homemate.badge.service.BadgeService;
import com.zerobase.homemate.badge.service.UserBadgeStatsService;
import com.zerobase.homemate.entity.*;
import com.zerobase.homemate.entity.enums.*;
import com.zerobase.homemate.repository.BadgeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class BadgeServiceTest {

    private BadgeRepository badgeRepository;
    private UserBadgeStatsService userBadgeStatsService;
    private BadgeService badgeService;

    private User user;

    @BeforeEach
    void setUp() {
        badgeRepository = mock(BadgeRepository.class);
        userBadgeStatsService = mock(UserBadgeStatsService.class);

        badgeService = new BadgeService(badgeRepository, userBadgeStatsService);

        user = User.builder()
                .id(1L)
                .profileName("mockUser")
                .userRole(UserRole.USER)
                .userStatus(UserStatus.ACTIVE)
                .build();
    }

    private Chore createChore(User user, Space space, String title) {
        return Chore.builder()
                .user(user)
                .space(space)
                .title(title)
                .repeatType(RepeatType.DAILY)
                .repeatInterval(1)
                .notificationYn(true)
                .notificationTime(LocalTime.of(19, 0))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now())
                .build();
    }

    @Test
    @DisplayName("공간/제목 배지 획득 테스트")
    void evaluateBadges_shouldAwardSpaceAndTitleBadges() {
        // given
        Chore bathroomChore = createChore(user, Space.BATHROOM, "샤워 후 물기 제거");
        Chore laundryChore = createChore(user, Space.ETC, "빨래하기");

        when(badgeRepository.existsByUserAndBadgeType(user, BadgeType.BEGINNER_BATHROOM)).thenReturn(false);
        when(badgeRepository.existsByUserAndBadgeType(user, BadgeType.SEED_LAUNDRY)).thenReturn(false);

        when(userBadgeStatsService.getSpaceCount(user.getId(), Space.BATHROOM)).thenReturn(30L);
        when(userBadgeStatsService.getTitleCount(user.getId(), "빨래하기")).thenReturn(30L);

        // when
        badgeService.evaluateBadges(user, bathroomChore);
        badgeService.evaluateBadges(user, laundryChore);

        // then
        ArgumentCaptor<List<Badge>> captor = ArgumentCaptor.forClass(List.class);
        verify(badgeRepository, atLeastOnce()).saveAll(captor.capture());

        List<Badge> savedBadges = captor.getAllValues().stream().flatMap(List::stream).toList();

        assertTrue(savedBadges.stream().anyMatch(b -> b.getBadgeType() == BadgeType.BEGINNER_BATHROOM));
        assertTrue(savedBadges.stream().anyMatch(b -> b.getBadgeType() == BadgeType.SEED_LAUNDRY));
    }

    @Test
    @DisplayName("등록 배지 획득 테스트")
    void evaluateBadgesOnCreate_shouldAwardRegisterBadges() {
        // given
        when(badgeRepository.existsByUserAndBadgeType(user, BadgeType.SMALL_J)).thenReturn(false);
        when(userBadgeStatsService.getTotalRegisteredCount(user.getId())).thenReturn(30L);

        // when
        badgeService.evaluateBadgesOnCreate(user);

        // then
        ArgumentCaptor<List<Badge>> captor = ArgumentCaptor.forClass(List.class);
        verify(badgeRepository, atLeastOnce()).saveAll(captor.capture());

        List<Badge> savedBadges = captor.getValue();
        assertTrue(savedBadges.stream().anyMatch(b -> b.getBadgeType() == BadgeType.SMALL_J));
    }

    @Test
    @DisplayName("Closest badges 반환 테스트")
    void getClosestBadges_shouldReturnProperBadges() {
        // given
        when(userBadgeStatsService.getTotalCompletedCount(user.getId())).thenReturn(30L);
        when(userBadgeStatsService.getSpaceCount(user.getId(), Space.BATHROOM)).thenReturn(30L);
        when(userBadgeStatsService.getTitleCount(user.getId(), "빨래하기")).thenReturn(30L);

        when(badgeRepository.findAllByUserId(user.getId())).thenReturn(List.of());

        // when
        List<BadgeProgressResponse> closest = badgeService.getClosestBadges(user.getId());

        // then
        assertTrue(closest.stream().allMatch(b -> !b.acquired() || b.remainingCount() == 0));
    }

    @Test
    @DisplayName("미션 배지 획득 테스트")
    void evaluateBadgesMission_shouldAwardMissionBadges() {
        // given
        when(badgeRepository.existsByUserAndBadgeType(user, BadgeType.SEED_MISSION))
                .thenReturn(false);

        when(userBadgeStatsService.getTotalMissionCount(user.getId()))
                .thenReturn(10L);  // 예: 미션 10회 완료 시 SEED_MISSION 부여 조건 충족

        // when
        badgeService.evaluateBadgesMission(user);

        // then
        ArgumentCaptor<List<Badge>> captor = ArgumentCaptor.forClass(List.class);
        verify(badgeRepository).saveAll(captor.capture());

        List<Badge> saved = captor.getValue();
        assertTrue(saved.stream()
                .anyMatch(b -> b.getBadgeType() == BadgeType.SEED_MISSION));
    }
}
