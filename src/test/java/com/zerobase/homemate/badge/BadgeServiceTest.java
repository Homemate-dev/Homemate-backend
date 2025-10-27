package com.zerobase.homemate.badge;


import com.zerobase.homemate.badge.service.BadgeService;
import com.zerobase.homemate.badge.service.UserBadgeStatsService;
import com.zerobase.homemate.entity.ChoreInstance;
import com.zerobase.homemate.entity.Mission;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.UserMission;
import com.zerobase.homemate.entity.enums.BadgeType;
import com.zerobase.homemate.entity.enums.MissionType;
import com.zerobase.homemate.entity.enums.UserRole;
import com.zerobase.homemate.entity.enums.UserStatus;
import com.zerobase.homemate.mission.service.MissionAssignmentService;
import com.zerobase.homemate.mission.service.MissionService;
import com.zerobase.homemate.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BadgeService30CompletionTest {

    private BadgeRepository badgeRepository;
    private UserRepository userRepository;
    private UserBadgeStatsService userBadgeStatsService;
    private BadgeService badgeService;
    private MissionService missionService;
    private final MissionRepository missionRepository;
    private final UserMissionRepository userMissionRepository;
    private final MissionAssignmentService missionAssignmentService;
    private final MissionProgressRepository missionProgressRepository;

    private User user;
    private UserMission userMission;
    private ChoreInstance choreInstance;

    BadgeService30CompletionTest(MissionRepository missionRepository, UserMissionRepository userMissionRepository, MissionAssignmentService missionAssignmentService, MissionProgressRepository missionProgressRepository) {
        this.missionRepository = missionRepository;
        this.userMissionRepository = userMissionRepository;
        this.missionAssignmentService = missionAssignmentService;
        this.missionProgressRepository = missionProgressRepository;
    }

    @BeforeEach
    void setUp() {
        badgeRepository = mock(BadgeRepository.class);
        userRepository = mock(UserRepository.class);
        userBadgeStatsService = mock(UserBadgeStatsService.class);
        badgeService = new BadgeService(badgeRepository, userRepository, userBadgeStatsService);

        missionService = new MissionService(
                missionRepository,
                userMissionRepository,
                missionAssignmentService,
                missionProgressRepository,
                userBadgeStatsService,
                badgeService
        );

        user = User.builder()
                .id(1L)
                .profileName("user")
                .userRole(UserRole.USER)
                .userStatus(UserStatus.ACTIVE)
                .build();

        Mission mission = Mission.builder()
                .id(1L)
                .missionType(MissionType.USER_ACTION)
                .build();

        userMission = UserMission.builder()
                .id(1L)
                .user(user)
                .mission(mission)
                .currentCount(29)
                .build();

        choreInstance = ChoreInstance.builder()
                .id(100L)
                .build();
    }

    @Test
    void evaluateBadges_shouldAwardAll30CountBadges() {
        // given
        BadgeType[] targetBadges = {
                BadgeType.START_HALF,         // 완료 배지
                BadgeType.SMALL_J,            // 등록 배지
                BadgeType.BEGINNER_BATHROOM,  // 공간 배지
                BadgeType.SEED_LAUNDRY        // 제목 배지
        };

        for (BadgeType badge : targetBadges) {
            when(badgeRepository.existsByUserAndBadgeType(user, badge)).thenReturn(false);
        }

        // Redis 기반 통계 값 모킹: 30회 완료
        when(userBadgeStatsService.getCount(user.getId())).thenReturn(30L);
        when(userBadgeStatsService.getSpaceCount(user.getId(), "BATHROOM")).thenReturn(30L);
        when(userBadgeStatsService.getTitleCount(user.getId(), "빨래하기")).thenReturn(30L);

        // when
        badgeService.evaluateBadges(user); // 완료 배지 & 공간/제목 배지
        badgeService.evaluateBadgesOnCreate(user, null); // 등록 배지

        // then
        ArgumentCaptor<com.zerobase.homemate.entity.Badge> captor = ArgumentCaptor.forClass(com.zerobase.homemate.entity.Badge.class);
        verify(badgeRepository, atLeastOnce()).save(captor.capture());

        List<com.zerobase.homemate.entity.Badge> savedBadges = captor.getAllValues();
        assertTrue(savedBadges.stream().anyMatch(b -> b.getBadgeType() == BadgeType.START_HALF));
        assertTrue(savedBadges.stream().anyMatch(b -> b.getBadgeType() == BadgeType.SMALL_J));
        assertTrue(savedBadges.stream().anyMatch(b -> b.getBadgeType() == BadgeType.BEGINNER_BATHROOM));
        assertTrue(savedBadges.stream().anyMatch(b -> b.getBadgeType() == BadgeType.SEED_LAUNDRY));
    }

    @Test
    void getClosestBadges_shouldReturnRemainingAsZeroAfter30Completions() {
        // given
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
        when(badgeRepository.findAllByUser(user)).thenReturn(Collections.emptyList());

        when(userBadgeStatsService.getCount(user.getId())).thenReturn(30L);
        when(userBadgeStatsService.getSpaceCount(user.getId(), "BATHROOM")).thenReturn(30L);
        when(userBadgeStatsService.getTitleCount(user.getId(), "빨래하기")).thenReturn(30L);

        // when
        List<BadgeResponse> closest = badgeService.getClosestBadges(1L);

        // then
        when(userBadgeStatsService.getCount(user.getId())).thenReturn(30L); // 전체 배지
        when(userBadgeStatsService.getSpaceCount(user.getId(), "BATHROOM")).thenReturn(30L);
        when(userBadgeStatsService.getTitleCount(user.getId(), "빨래하기")).thenReturn(30L);

    }

//    @Test
//    void whenMissionCompleted_ThenBadgeServiceShouldBeTriggered() {
//        // given
//        when(badgeRepository.existsByUserAndBadgeType(any(), any())).thenReturn(false);
//        when(userBadgeStatsService.getCount(user.getId())).thenReturn(30L);
//
//        // when
//        missionService.applyCompletion(userMission, null, choreInstance, new java.util.ArrayList<>(), null);
//
//        // then
//        verify(userBadgeStatsService, times(1)).incrementMissionCount(user.getId());
//        verify(badgeRepository, atLeastOnce()).save(any());
//    }


}
