package com.zerobase.homemate.recommend;

import com.zerobase.homemate.badge.service.UserBadgeStatsService;
import com.zerobase.homemate.chore.dto.ChoreInstanceDto;
import com.zerobase.homemate.entity.*;
import com.zerobase.homemate.entity.enums.*;
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
        List<ChoreInstanceDto.Response> response =
                categoryChoreCreator.createChoreFromCategory(userId, Category.WINTER, categoryChoreId);

        // then
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals("청소하기", response.get(0).getTitleSnapshot());
        verify(choreRepository).save(any(Chore.class));
        verify(choreInstanceRepository).saveAll(anyList());
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


        Chore chore = Chore.builder()
                .id(100L)
                .title("청소하기")
                .build();

        when(choreInstanceGenerator.generateInstances(any())).thenReturn(List.of(
                ChoreInstance.builder()
                        .id(1L)
                        .chore(chore) // 여기 추가
                        .titleSnapshot(chore.getTitle())
                        .dueDate(LocalDate.now())
                        .notificationTime(LocalTime.of(17, 0))
                        .choreStatus(ChoreStatus.PENDING)
                        .build()
        ));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryChoreRepository.findById(1L)).thenReturn(Optional.of(template));
        when(choreRepository.findByUserIdAndTitle(1L, template.getTitle())).thenReturn(Optional.empty());
        when(choreRepository.save(any(Chore.class))).thenAnswer(inv -> inv.getArguments()[0]);
        when(userNotificationSettingRepository.findByUserId(anyLong()))
                .thenReturn(Optional.empty()); // 기본값 사용
        when(missionService.increaseMissionCountForAction(eq(user.getId()),
                eq(UserActionType.CREATE_CHORE_RECOMMENDED)))
                .thenReturn(List.of());

        // when
        List<ChoreInstanceDto.Response> response =
                categoryChoreCreator.createChoreFromCategory(1L, Category.WINTER, 1L);

        // then
        assertNotNull(response.get(0).getNotificationTime(),
                "알림 시간은 기본값으로 세팅되어 있어야 한다");
        assertEquals(LocalTime.of(17, 0), response.get(0).getNotificationTime(),
                "기본 알림 시간은 17:00");
        verify(choreInstanceRepository).saveAll(anyList());
    }



}
