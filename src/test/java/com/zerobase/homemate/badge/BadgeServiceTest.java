package com.zerobase.homemate.badge;

import com.zerobase.homemate.badge.service.BadgeCondition;
import com.zerobase.homemate.badge.service.BadgeService;
import com.zerobase.homemate.badge.service.UserBadgeStatsService;
import com.zerobase.homemate.entity.Badge;
import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.enums.*;
import com.zerobase.homemate.repository.BadgeRepository;
import com.zerobase.homemate.repository.CategoryChoreRepository;
import com.zerobase.homemate.repository.ChoreRepository;
import com.zerobase.homemate.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BadgeServiceTest {

    private BadgeRepository badgeRepository;
    private BadgeService badgeService;
    private ChoreRepository choreRepository;
    private CategoryChoreRepository categoryChoreRepository;
    private User user;
    private UserRepository userRepository;
    private UserBadgeStatsService userBadgeStatsService;

    @BeforeEach
    void setUp() {
        badgeRepository = Mockito.mock(BadgeRepository.class);
        choreRepository = Mockito.mock(ChoreRepository.class);
        categoryChoreRepository = Mockito.mock(CategoryChoreRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        badgeService = new BadgeService(badgeRepository, choreRepository, categoryChoreRepository, userRepository, userBadgeStatsService); // choreRepository, categoryChoreRepository는 countRemaining에서 Mock 처리 필요 시 추가
        user = User.builder()
                .id(1L)
                .profileName("test")
                .userStatus(UserStatus.ACTIVE)
                .userRole(UserRole.USER)
                .build();

        when(choreRepository.countByUserAndSpaceAndIsCompletedTrue(any(), any())).thenReturn(3L);

        when(choreRepository.countByUserAndTitleAndIsCompletedTrue(any(), any())).thenReturn(2L);

        when(categoryChoreRepository.existsByChoreAndCategory(any(), any())).thenReturn(false);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    }

    @Test
    @DisplayName("획득 배지가 먼저, 잠금 배지가 뒤, 가나다순으로 정렬되어 반환된다")
    void testGetAcquiredBadgesSorted() {
        // given: user가 이미 획득한 배지 하나
        Badge acquiredBadge = Badge.builder()
                .user(user)
                .badgeType(BadgeType.START_HALF)
                .build();
        when(badgeRepository.findAllByUser(user))
                .thenReturn(List.of(acquiredBadge));

        // when
        List<BadgeResponse> result = badgeService.getAcquiredBadges(user.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();

        // 1.  획득 배지가 앞에 오는지 확인
        assertThat(result.get(0).acquired()).isTrue();

        // 2. 획득 여부 순서: true -> false
        boolean allAcquiredFirst = true;
        for (BadgeResponse b : result) {
            if (!b.acquired()) {
                allAcquiredFirst = false;
                break;
            }
        }
        assertThat(allAcquiredFirst).isFalse(); // false 나오는 지점부터 잠금 배지

        // 3. 각 그룹 내 가나다순 확인
        List<BadgeResponse> acquiredGroup = result.stream()
                .filter(BadgeResponse::acquired)
                .toList();
        for (int i = 0; i < acquiredGroup.size() - 1; i++) {
            String curr = acquiredGroup.get(i).type().getBadgeName();
            String next = acquiredGroup.get(i + 1).type().getBadgeName();
            assertThat(curr.compareTo(next)).isLessThanOrEqualTo(0);
        }

        List<BadgeResponse> lockedGroup = result.stream()
                .filter(b -> !b.acquired())
                .toList();
        for (int i = 0; i < lockedGroup.size() - 1; i++) {
            String curr = lockedGroup.get(i).type().getBadgeName();
            String next = lockedGroup.get(i + 1).type().getBadgeName();
            assertThat(curr.compareTo(next)).isLessThanOrEqualTo(0);
        }

        // 4. 남은 횟수는 0 이상
        for (BadgeResponse b : result) {
            assertThat(b.remainingCount()).isGreaterThanOrEqualTo(0);
        }
    }

    @Test
    @DisplayName("남은 횟수가 가장 적은 3개의 배지를 반환한다")
    void testGetClosestBadges() {
        // given: user가 이미 START_HALF 배지를 획득
        Badge acquiredBadge = Badge.builder()
                .user(user)
                .badgeType(BadgeType.START_HALF)
                .build();
        when(badgeRepository.findAllByUser(user))
                .thenReturn(List.of(acquiredBadge));

        // choreRepository에서 남은 횟수 계산용 Mock
        when(choreRepository.countByUserAndSpaceAndIsCompletedTrue(any(), any())).thenReturn(3L);
        when(choreRepository.countByUserAndTitleAndIsCompletedTrue(any(), any())).thenReturn(2L);

        // when
        List<BadgeResponse> result = badgeService.getClosestBadges(user.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isLessThanOrEqualTo(3);

        // 남은 횟수 순서 확인
        for (int i = 0; i < result.size() - 1; i++) {
            assertThat(result.get(i).remainingCount())
                    .isLessThanOrEqualTo(result.get(i + 1).remainingCount());
        }

        // 획득한 배지는 포함되지 않아야 한다.
        assertThat(result.stream().anyMatch(BadgeResponse::acquired)).isFalse();
    }

    private void assertBadgeAcquired(User user, Runnable action, BadgeType expectedBadgeType) {
        // when
        action.run();

        // then
        ArgumentCaptor<Badge> captor = ArgumentCaptor.forClass(Badge.class);
        verify(badgeRepository, atLeastOnce()).save(captor.capture());

        boolean found = captor.getAllValues().stream()
                .anyMatch(b -> b.getBadgeType() == expectedBadgeType);

        assertThat(found).isTrue();
    }

    @Test
    @DisplayName("바닥 청소기 배지 획득 테스트")
    void testFloorCleanerBadge() {
        when(choreRepository.countByUserAndTitleAndIsCompletedTrue(user, "바닥 청소기 돌리기"))
                .thenReturn(30L);

        assertBadgeAcquired(user, () -> badgeService.evaluateBadges(user), BadgeType.BEGINNER_FAIRY);
    }

    @Test
    @DisplayName("KITCHEN 공간 집안일 배지 획득 테스트")
    void testKitchenSpaceBadge() {
        when(choreRepository.countByUserAndSpaceAndIsCompletedTrue(user, Space.KITCHEN))
                .thenReturn(30L);

        assertBadgeAcquired(user, () -> badgeService.evaluateBadges(user), BadgeType.BEGINNER_KITCHEN);
    }

    @Test
    @DisplayName("집안일 30회 등록 배지 획득 테스트")
    void testChore30TimesBadge() {
        Chore chore = Chore.builder()
                .user(user)
                .space(Space.ETC)
                .title("Sample Chore")
                .build();

        // BadgeCondition을 Mock 처리
        BadgeCondition mockCondition = Mockito.mock(BadgeCondition.class);
        when(mockCondition.matchesCondition(chore)).thenReturn(true);

        // BadgeService에서 createCondition 호출 시 mockCondition 반환
        BadgeService spyService = Mockito.spy(badgeService);
        doReturn(mockCondition).when(spyService).createCondition(BadgeType.SMALL_J);

        // 실행
        spyService.evaluateBadgesOnCreate(user, chore);

        // saveAll 호출 확인
        ArgumentCaptor<List<Badge>> captor = ArgumentCaptor.forClass(List.class);
        verify(badgeRepository).saveAll(captor.capture());
        List<Badge> savedBadges = captor.getValue();

        assertThat(savedBadges).hasSize(1);
        assertThat(savedBadges.get(0).getBadgeType()).isEqualTo(BadgeType.SMALL_J);
    }


}
