package com.zerobase.homemate.recommend;

import com.zerobase.homemate.badge.service.UserBadgeStatsService;
import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.ChoreInstance;
import com.zerobase.homemate.entity.SpaceChore;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.enums.Category;
import com.zerobase.homemate.entity.enums.RepeatType;
import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.entity.enums.UserActionType;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.mission.service.MissionService;
import com.zerobase.homemate.recommend.service.SpaceChoreCreator;
import com.zerobase.homemate.recommend.service.stats.RedisChoreStatsService;
import com.zerobase.homemate.repository.*;
import com.zerobase.homemate.util.ChoreInstanceGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class SpaceChoreCreatorTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChoreRepository choreRepository;

    @Mock
    private SpaceChoreRepository spaceChoreRepository;

    @Mock
    private ChoreInstanceRepository choreInstanceRepository;

    @Mock
    private ChoreInstanceGenerator choreInstanceGenerator;

    @Mock
    private CategoryChoreRepository categoryChoreRepository;

    @InjectMocks
    private SpaceChoreCreator spaceChoreCreator;

    @Mock
    private UserNotificationSettingRepository userNotificationSettingRepository;

    @Mock
    private RedisChoreStatsService redisChoreStatsService;

    @Mock
    private MissionService missionService;

    @Mock
    private UserBadgeStatsService userBadgeStatsService;

    @Test
    @DisplayName("SpaceChore 기반 집안일 등록 성공")
    void createChoreFromSpace_success() {
        Long userId = 1L;
        Long spaceChoreId = 10L;

        User user = User.builder().id(userId).build();
        SpaceChore template = SpaceChore.builder()
                .titleKo("주방 싱크대 정리하기")
                .space(Space.KITCHEN)
                .repeatType(RepeatType.DAILY)
                .repeatInterval(1)
                .build();
        Chore chore = Chore.builder()
                .id(100L)
                .user(user)
                .title("주방 싱크대 정리하기")
                .space(Space.KITCHEN)
                .repeatType(RepeatType.DAILY)
                .repeatInterval(1)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .notificationYn(true)
                .notificationTime(LocalTime.of(9,0))
                .isDeleted(false)
                .build();

        ChoreInstance instance = ChoreInstance.builder()
                .id(1L)
                .chore(chore)
                .titleSnapshot(chore.getTitle())
                .dueDate(LocalDate.now())
                .notificationTime(LocalTime.of(9,0))
                .build();

        // Mocking
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(spaceChoreRepository.findById(spaceChoreId)).thenReturn(Optional.of(template));
        when(choreInstanceRepository.existsByUserIdAndTitle(userId, template.getTitleKo())).thenReturn(false);
        when(choreRepository.findByUserIdAndTitle(userId, template.getTitleKo())).thenReturn(Optional.of(chore));
        when(choreInstanceGenerator.generateInstances(chore)).thenReturn(List.of(instance));
        when(missionService.increaseMissionCountForAction(userId, UserActionType.CREATE_CHORE_WITH_SPACE))
                .thenReturn(List.of());

        // 실행
        var response = spaceChoreCreator.createChoreFromSpace(userId, Space.KITCHEN, spaceChoreId);

        // 검증
        assertNotNull(response);
        assertEquals(1, response.getData().size());
        assertEquals("주방 싱크대 정리하기", response.getData().get(0).getTitleSnapshot());
        verify(choreInstanceRepository).saveAll(List.of(instance));
        verify(redisChoreStatsService).increment(any(Category.class), eq(Space.KITCHEN));
        verify(userBadgeStatsService).incrementRegisterCount(userId);
    }

    @Test
    @DisplayName("이미 등록된 추천 집안일 재등록 시 실패")
    void createChoreFromSpace_shouldFailWhenAlreadyExists() {
        Long userId = 1L;
        Long spaceChoreId = 10L;

        User user = User.builder().id(userId).build();
        SpaceChore template = SpaceChore.builder()
                .titleKo("주방 싱크대 정리하기")
                .space(Space.KITCHEN)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(spaceChoreRepository.findById(spaceChoreId)).thenReturn(Optional.of(template));
        when(choreInstanceRepository.existsByUserIdAndTitle(userId, template.getTitleKo())).thenReturn(true);

        CustomException exception = assertThrows(CustomException.class,
                () -> spaceChoreCreator.createChoreFromSpace(userId, Space.KITCHEN, spaceChoreId));

        assertEquals(ErrorCode.CHORE_ALREADY_REGISTERED, exception.getErrorCode());

        verify(choreRepository, never()).save(any());
        verify(choreInstanceRepository, never()).saveAll(anyList());
    }
}
