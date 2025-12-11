package com.zerobase.homemate.badge;

import com.zerobase.homemate.badge.service.BadgeCacheService;
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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class BadgeServiceTest {

    private BadgeRepository badgeRepository;
    private UserBadgeStatsService userBadgeStatsService;
    private BadgeCacheService badgeCacheService;
    private BadgeService badgeService;

    private User user;

    @BeforeEach
    void setUp() {
        badgeRepository = mock(BadgeRepository.class);
        userBadgeStatsService = mock(UserBadgeStatsService.class);
        badgeCacheService = mock(BadgeCacheService.class);

        badgeService = new BadgeService(
                badgeRepository,
                userBadgeStatsService,
                badgeCacheService
        );

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

        when(badgeRepository.findAllByUserId(user.getId())).thenReturn(List.of());
        when(userBadgeStatsService.getSpaceCount(user.getId(), Space.BATHROOM)).thenReturn(30L);
        when(userBadgeStatsService.getTitleCount(user.getId(), "빨래하기")).thenReturn(30L);
        when(userBadgeStatsService.getTotalCompletedCount(user.getId())).thenReturn(30L);

        // when
        badgeService.evaluateBadges(user, bathroomChore);
        badgeService.evaluateBadges(user, laundryChore);

        // then
        ArgumentCaptor<List<Badge>> captor = ArgumentCaptor.forClass(List.class);
        verify(badgeRepository, atLeastOnce()).saveAll(captor.capture());
        verify(badgeCacheService, atLeastOnce()).evictClosestBadges(user.getId());

        List<Badge> saved = captor.getAllValues()
                .stream().flatMap(List::stream).toList();

        assertTrue(saved.stream().anyMatch(b -> b.getBadgeType().getCategory() == BadgeCategory.SPACE));
        assertTrue(saved.stream().anyMatch(b -> b.getBadgeType().getCategory() == BadgeCategory.TITLE));
    }

    @Test
    @DisplayName("등록 배지 획득 테스트")
    void evaluateBadgesOnCreate_shouldAwardRegisterBadges() {
        when(userBadgeStatsService.getTotalRegisteredCount(user.getId())).thenReturn(30L);
        when(badgeRepository.existsByUserAndBadgeType(any(), any())).thenReturn(false);

        badgeService.evaluateBadgesOnCreate(user);

        ArgumentCaptor<List<Badge>> captor = ArgumentCaptor.forClass(List.class);
        verify(badgeRepository).saveAll(captor.capture());
        verify(badgeCacheService).evictClosestBadges(user.getId());

        List<Badge> saved = captor.getValue();
        assertTrue(saved.stream().allMatch(b -> b.getBadgeType().getCategory() == BadgeCategory.REGISTER));
    }

    @Test
    @DisplayName("미션 배지 획득 테스트")
    void evaluateBadgesMission_shouldAwardMissionBadges() {
        when(userBadgeStatsService.getTotalMissionCount(user.getId())).thenReturn(10L);
        when(badgeRepository.existsByUserAndBadgeType(any(), any())).thenReturn(false);

        badgeService.evaluateBadgesMission(user);

        ArgumentCaptor<List<Badge>> captor = ArgumentCaptor.forClass(List.class);
        verify(badgeRepository).saveAll(captor.capture());
        verify(badgeCacheService).evictClosestBadges(user.getId());

        List<Badge> saved = captor.getValue();
        assertTrue(saved.stream().allMatch(b -> b.getBadgeType().getCategory() == BadgeCategory.MISSION));
    }

    @Test
    @DisplayName("Closest badges 반환 테스트")
    void getClosestBadges_shouldReturnProperBadges() {
        when(badgeRepository.findAllByUserId(user.getId())).thenReturn(List.of());
        when(userBadgeStatsService.getTotalCompletedCount(user.getId())).thenReturn(30L);

        List<BadgeProgressResponse> result = badgeService.getClosestBadges(user.getId());

        assertTrue(result.stream().noneMatch(BadgeProgressResponse::acquired));
    }

    @Test
    @DisplayName("캐시 HIT 시 DB 조회 없이 캐시값 반환")
    void getClosestBadgesCached_hit() {
        List<BadgeProgressResponse> cached = List.of(
                BadgeProgressResponse.of(BadgeType.SEED_MISSION, 5, false, null)
        );

        when(badgeCacheService.getCachedClosestBadges(user.getId())).thenReturn(cached);

        List<BadgeProgressResponse> result = badgeService.getClosestBadgesCached(user.getId());

        assertSame(result, cached);
        verifyNoInteractions(badgeRepository);
    }

    @Test
    @DisplayName("캐시 MISS 시 계산 후 저장")
    void getClosestBadgesCached_miss() {
        when(badgeCacheService.getCachedClosestBadges(user.getId())).thenReturn(null);
        when(badgeRepository.findAllByUserId(user.getId())).thenReturn(List.of());
        when(userBadgeStatsService.getTotalCompletedCount(user.getId())).thenReturn(10L);

        badgeService.getClosestBadgesCached(user.getId());

        verify(badgeCacheService).cacheClosestBadges(eq(user.getId()), anyList());
    }
}
