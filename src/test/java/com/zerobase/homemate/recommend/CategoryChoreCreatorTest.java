package com.zerobase.homemate.recommend;

import com.zerobase.homemate.badge.service.UserBadgeStatsService;
import com.zerobase.homemate.chore.dto.ChoreDto.ApiResponse;
import com.zerobase.homemate.chore.dto.ChoreInstanceDto;
import com.zerobase.homemate.entity.*;
import com.zerobase.homemate.entity.enums.*;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.mission.service.MissionService;
import com.zerobase.homemate.recommend.service.CategoryChoreCreator;
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

@ExtendWith(MockitoExtension.class)
public class CategoryChoreCreatorTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChoreRepository choreRepository;

    @Mock
    private CategoryChoreRepository categoryChoreRepository;

    @Mock
    private ChoreInstanceRepository choreInstanceRepository;

    @Mock
    private SpaceChoreRepository spaceChoreRepository;

    @Mock
    private ChoreInstanceGenerator choreInstanceGenerator;

    @InjectMocks
    private CategoryChoreCreator categoryChoreCreator;

    @Mock
    private UserNotificationSettingRepository userNotificationSettingRepository;

    @Mock
    private RedisChoreStatsService redisChoreStatsService;

    @Mock
    private MissionService missionService;

    @Mock
    private UserBadgeStatsService userBadgeStatsService;

    @Test
    @DisplayName("CategoryChore 기반 집안일 등록 테스트 - Space 매칭")
    void createChoreFromCategory_shouldCreateChoreWithMatchedSpace() {
        // given
        Long userId = 1L;
        Long categoryChoreId = 1L;

        User user = User.builder().id(userId).build();

        CategoryChore categoryChore = CategoryChore.builder()
                .category(Category.WINTER)
                .title("청소하기")
                .repeatType(RepeatType.DAILY)
                .repeatInterval(3)
                .build();

        SpaceChore spaceChore = SpaceChore.builder()
                .space(Space.KITCHEN)
                .titleKo("청소하기")
                .code("주방")
                .repeatType(RepeatType.DAILY)
                .repeatInterval(3)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryChoreRepository.findById(categoryChoreId)).thenReturn(Optional.of(categoryChore));
        when(spaceChoreRepository.findByTitleKo(categoryChore.getTitle())).thenReturn(Optional.of(spaceChore));
        when(choreRepository.save(any(Chore.class))).thenAnswer(inv -> inv.getArguments()[0]);
        when(choreInstanceGenerator.generateInstances(any(Chore.class))).thenReturn(List.of(
                ChoreInstance.builder()
                        .id(1L)
                        .titleSnapshot(categoryChore.getTitle())
                        .dueDate(LocalDate.now())
                        .chore(Chore.builder().id(10L).build())
                        .build()
        ));
        when(missionService.increaseMissionCountForAction(eq(userId), eq(UserActionType.CREATE_CHORE_RECOMMENDED)))
                .thenReturn(List.of());

        // when
        ApiResponse<List<ChoreInstanceDto.Response>> response =
                categoryChoreCreator.createChoreFromCategory(userId, Category.WINTER, categoryChoreId);

        // then
        assertNotNull(response.getData());
        assertFalse(response.getData().isEmpty());
        assertEquals("청소하기", response.getData().get(0).getTitleSnapshot());
        verify(choreRepository).save(any(Chore.class));
        verify(choreInstanceRepository).saveAll(anyList());
    }


    @Test
    @DisplayName("이미 등록된 추천 집안일 재등록 시 실패")
    void createChoreFromCategory_shouldFailWhenAlreadyExists() {
        // given
        Long userId = 1L;
        Long categoryChoreId = 1L;

        User user = User.builder().id(userId).build();

        CategoryChore categoryChore = CategoryChore.builder()
                .category(Category.WINTER)
                .title("청소하기")
                .repeatType(RepeatType.DAILY)
                .repeatInterval(3)
                .build();

        // 이미 해당 유저가 동일한 제목의 chore를 가지고 있다고 가정
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryChoreRepository.findById(categoryChoreId)).thenReturn(Optional.of(categoryChore));
        when(choreRepository.existsByUserIdAndTitle(userId, categoryChore.getTitle())).thenReturn(true);

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            categoryChoreCreator.createChoreFromCategory(userId, Category.WINTER, categoryChoreId);
        });

        // then
        assertEquals(ErrorCode.CHORE_ALREADY_REGISTERED, exception.getErrorCode());
        verify(choreRepository, never()).save(any(Chore.class));
        verify(choreInstanceRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("notificationTime 기본값 세팅 확인")
    void createChore_shouldSetDefaultNotificationTimeEvenIfDisabled() {
        // given
        User user = User.builder().id(1L).build();
        CategoryChore template = CategoryChore.builder()
                .title("청소하기")
                .repeatType(RepeatType.DAILY)
                .repeatInterval(1)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryChoreRepository.findById(1L)).thenReturn(Optional.of(template));
        when(choreRepository.save(any(Chore.class))).thenAnswer(inv -> inv.getArguments()[0]);
        when(choreInstanceGenerator.generateInstances(any())).thenReturn(List.of(
                ChoreInstance.builder()
                        .id(1L)
                        .titleSnapshot("청소하기")
                        .dueDate(LocalDate.now())
                        .notificationTime(LocalTime.of(19, 0))
                        .choreStatus(ChoreStatus.PENDING)
                        .build()
        ));
        when(userNotificationSettingRepository.findByUserId(anyLong()))
                .thenReturn(Optional.empty()); // 기본값 사용
        when(missionService.increaseMissionCountForAction(eq(user.getId()),
                eq(UserActionType.CREATE_CHORE_RECOMMENDED)))
                .thenReturn(List.of());

        // when
        ApiResponse<List<ChoreInstanceDto.Response>> response =
                categoryChoreCreator.createChoreFromCategory(1L, Category.WINTER, 1L);

        // then
        assertNotNull(response.getData().get(0).getNotificationTime(),
                "알림 시간은 기본값으로 세팅되어 있어야 한다");
        assertEquals(LocalTime.of(9, 0), response.getData().get(0).getNotificationTime(),
                "기본 알림 시간은 09:00");
    }


}
