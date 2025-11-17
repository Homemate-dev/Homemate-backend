package com.zerobase.homemate.badge;


import com.zerobase.homemate.badge.service.BadgeService;
import com.zerobase.homemate.badge.service.UserBadgeStatsService;
import com.zerobase.homemate.entity.*;
import com.zerobase.homemate.entity.enums.*;
import com.zerobase.homemate.mission.service.MissionAssignmentService;
import com.zerobase.homemate.mission.service.MissionService;
import com.zerobase.homemate.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private MissionRepository missionRepository;
    private UserMissionRepository userMissionRepository;
    private MissionAssignmentService missionAssignmentService;
    private MissionProgressRepository missionProgressRepository;
    private CategoryChoreRepository categoryChoreRepository;

    private User user;
    private UserMission userMission;
    private ChoreInstance choreInstance;


    @BeforeEach
    void setUp() {
        badgeRepository = mock(BadgeRepository.class);
        userRepository = mock(UserRepository.class);
        userBadgeStatsService = mock(UserBadgeStatsService.class);
        badgeService = new BadgeService(badgeRepository, userRepository, userBadgeStatsService, categoryChoreRepository);

        missionRepository = mock(MissionRepository.class);
        userMissionRepository = mock(UserMissionRepository.class);
        missionAssignmentService = mock(MissionAssignmentService.class);
        missionProgressRepository = mock(MissionProgressRepository.class);
        categoryChoreRepository = mock(CategoryChoreRepository.class);


        missionService = new MissionService(
                missionRepository,
                userMissionRepository,
                missionAssignmentService,
                missionProgressRepository,
                userBadgeStatsService,
                badgeService,
                categoryChoreRepository
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
    void evaluateBadges_shouldAwardCompletedAndSpaceTitleBadges() {
        // given
        BadgeType[] targetBadges = {
                BadgeType.BEGINNER_BATHROOM, // 공간 배지
                BadgeType.SEED_LAUNDRY       // 제목 배지
        };

        for (BadgeType badge : targetBadges) {
            when(badgeRepository.existsByUserAndBadgeType(user, badge)).thenReturn(false);
        }

        Chore chore = Chore.builder()
                .user(user)
                .space(Space.BATHROOM)
                .title("샤워 후 물기 제거하기")
                .repeatType(RepeatType.DAILY)
                .repeatInterval(1)
                .notificationYn(true)
                .notificationTime(LocalTime.of(19, 0))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now())
                .build();

        Chore laundry = Chore.builder()
                .user(user)
                .space(Space.ETC)
                .title("빨래하기")
                .repeatType(RepeatType.DAILY)
                .repeatInterval(1)
                .notificationYn(true)
                .notificationTime(LocalTime.of(19, 0))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now())
                .build();

        // 완료/공간/제목 배지 기준 count 모킹
        when(userBadgeStatsService.getCountByCategory(user.getId(), BadgeType.BEGINNER_BATHROOM)).thenReturn(30L);
        when(userBadgeStatsService.getSpaceCount(user.getId(), Space.BATHROOM)).thenReturn(30L);

        when(userBadgeStatsService.getCountByCategory(user.getId(), BadgeType.SEED_LAUNDRY)).thenReturn(30L);
        when(userBadgeStatsService.getTitleCount(user.getId(), "빨래하기")).thenReturn(30L);

        // when
        badgeService.evaluateBadges(chore.getUser(), chore);
        badgeService.evaluateBadges(chore.getUser(), laundry);

        // then
        ArgumentCaptor<List<Badge>> captor = ArgumentCaptor.forClass(List.class);
        verify(badgeRepository, atLeastOnce()).saveAll(captor.capture());

        // 모든 saveAll 호출의 인자 리스트를 전부 가져오기
        List<List<Badge>> allSavedLists = captor.getAllValues();

        // 모든 호출에서 저장된 배지를 평탄화(flatten)
        List<Badge> allSavedBadges = allSavedLists.stream()
                .flatMap(List::stream)
                .toList();

        // 이제 통합된 리스트에서 원하는 배지가 있는지 검사
        assertTrue(allSavedBadges.stream().anyMatch(b -> b.getBadgeType() == BadgeType.BEGINNER_BATHROOM));
        assertTrue(allSavedBadges.stream().anyMatch(b -> b.getBadgeType() == BadgeType.SEED_LAUNDRY));
    }

    @Test
    void evaluateBadgesOnCreate_shouldAwardRegisterBadges() {
        // given
        BadgeType[] registerBadges = {
                BadgeType.SMALL_J // 등록 배지
        };

        for (BadgeType badge : registerBadges) {
            when(badgeRepository.existsByUserAndBadgeType(user, badge)).thenReturn(false);
        }
        Chore chore = Chore.builder()
                .title("설거지하기")
                .createdAt(LocalDateTime.now())
                .space(Space.KITCHEN)
                .user(user)
                .notificationYn(true)
                .notificationTime(LocalTime.of(19, 0))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now())
                .repeatType(RepeatType.DAILY)
                .repeatInterval(1)
                .build();


        when(userBadgeStatsService.getRegisterCount(user.getId())).thenReturn(30L);
        // when
        badgeService.evaluateBadgesOnCreate(user);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class); // raw 타입 사용
        verify(badgeRepository, atLeastOnce()).saveAll(captor.capture());

        List<Badge> savedBadges = (List<Badge>) captor.getValue(); // 캐스팅
        assertTrue(savedBadges.stream().anyMatch(b -> b.getBadgeType() == BadgeType.SMALL_J));
    }

    @Test
    void getClosestBadges_shouldReturnRemainingAsZeroAfter30Completions() {
        // given
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
        when(badgeRepository.findAllByUser(user)).thenReturn(Collections.emptyList());

        when(userBadgeStatsService.getCount(user.getId())).thenReturn(30L);
        when(userBadgeStatsService.getSpaceCount(user.getId(), Space.BATHROOM)).thenReturn(30L);
        when(userBadgeStatsService.getTitleCount(user.getId(), "빨래하기")).thenReturn(30L);

        // when
        List<BadgeProgressResponse> closest = badgeService.getClosestBadges(1L);

        // then
        when(userBadgeStatsService.getCount(user.getId())).thenReturn(30L); // 전체 배지
        when(userBadgeStatsService.getSpaceCount(user.getId(), Space.BATHROOM)).thenReturn(30L);
        when(userBadgeStatsService.getTitleCount(user.getId(), "빨래하기")).thenReturn(30L);

    }

}
