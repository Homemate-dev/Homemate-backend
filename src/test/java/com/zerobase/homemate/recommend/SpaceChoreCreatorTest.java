package com.zerobase.homemate.recommend;

import com.zerobase.homemate.badge.service.UserBadgeStatsService;
import com.zerobase.homemate.chore.dto.ChoreDto;
import com.zerobase.homemate.entity.*;
import com.zerobase.homemate.entity.enums.*;
import com.zerobase.homemate.mission.service.MissionService;
import com.zerobase.homemate.notification.component.ChoreInstanceCreatedEvent;
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
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    void createChoreFromSpace_success(){
        // given

        Long userId = 1L;
        Long spaceChoreId = 1L;

        User user = User.builder()
                .id(userId)
                .build();

        SpaceChore spaceChore = SpaceChore.builder()
                .space(Space.KITCHEN)
                .titleKo("주방 싱크대 정리하기")
                .repeatType(RepeatType.DAILY)
                .repeatInterval(1)
                .code("주방")
                .build();
        spaceChoreRepository.save(spaceChore);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(spaceChoreRepository.findById(spaceChoreId)).thenReturn(Optional.of(spaceChore));
        when(choreRepository.save(any(Chore.class))).thenAnswer(inv -> inv.getArguments()[0]);
        when(choreInstanceGenerator.generateInstances(any(Chore.class))).thenReturn(List.of());

        when(missionService.increaseMissionCountForAction(eq(userId), eq(
                UserActionType.CREATE_CHORE_WITH_SPACE)))
                .thenReturn(List.of());

        // when
        ChoreDto.ApiResponse<ChoreDto.Response> response = spaceChoreCreator.createChoreFromSpace(userId, spaceChoreId);

        // then
        assertEquals("주방 싱크대 정리하기", response.getData().getTitle());
        assertEquals(Space.KITCHEN, response.getData().getSpace());
        verify(choreRepository).save(any(Chore.class));
        verify(choreInstanceRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("동일 추천 집안일 재등록 시에도 새 Chore 및 Instance 생성")
    void createChoreFromSpace_shouldAlwaysCreateNewChore() {
        // given
        Long userId = 1L;
        Long spaceChoreId = 10L;

        User user = User.builder().id(userId).build();

        SpaceChore template = SpaceChore.builder()
                .titleKo("주방 정리하기")
                .space(Space.KITCHEN)
                .repeatType(RepeatType.DAILY)
                .repeatInterval(1)
                .build();

        Category category = Category.WEEKEND_WHOLE_ROUTINE;
        CategoryChore categoryChore = CategoryChore.builder()
                .title("주방 정리하기")
                .category(category)
                .build();

        UserNotificationSetting setting = UserNotificationSetting.createDefault(user, LocalTime.of(19, 0));

        Chore savedChore = Chore.builder()
                .id(100L)
                .title("주방 정리하기")
                .user(user)
                .space(Space.KITCHEN)
                .repeatType(RepeatType.DAILY)
                .repeatInterval(1)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .notificationTime(LocalTime.of(19, 0))
                .build();

        List<ChoreInstance> generatedInstances = List.of(
                ChoreInstance.builder().id(1L).chore(savedChore).dueDate(LocalDate.now()).build(),
                ChoreInstance.builder().id(2L).chore(savedChore).dueDate(LocalDate.now().plusDays(1)).build()
        );



        // Stubbing
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(spaceChoreRepository.findById(spaceChoreId)).thenReturn(Optional.of(template));
        when(userNotificationSettingRepository.findByUserId(userId)).thenReturn(Optional.of(setting));
        when(choreRepository.save(any(Chore.class))).thenReturn(savedChore);
        when(choreInstanceGenerator.generateInstances(savedChore)).thenReturn(generatedInstances);
        when(categoryChoreRepository.findByTitle("주방 정리하기"))
                .thenReturn(Optional.of(categoryChore)); // CategoryChore 객체를 반환하도록 설정

        // when
        ChoreDto.ApiResponse<ChoreDto.Response> response =
                spaceChoreCreator.createChoreFromSpace(userId, spaceChoreId);

        // then
        assertEquals("주방 정리하기", response.getData().getTitle());
        verify(choreRepository, times(1)).save(any());
        verify(choreInstanceRepository, times(1)).saveAll(anyList());
        verify(redisChoreStatsService, times(1)).increment(any(), any());
        verify(missionService, times(1)).increaseMissionCountForAction(eq(userId), any());
        verify(userBadgeStatsService, times(1)).incrementTotalRegistered(userId);
    }


    @Test
    @DisplayName("기본 설정이 없을 때, 알림은 ON 상태이며 시간은 19시로 설정된다.")
    void createChore_shouldSetDefaultNotificationTimeAndYn(){
        // given
        User user = User.builder().id(1L).build();

        SpaceChore template = SpaceChore.builder()
                .titleKo("주방 설거지하기")
                .repeatType(RepeatType.DAILY)
                .repeatInterval(1)
                .code("주방")
                .space(Space.KITCHEN)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(spaceChoreRepository.findById(anyLong())).thenReturn(Optional.of(template));
        when(choreRepository.save(any(Chore.class))).thenAnswer(inv -> inv.getArguments()[0]);
        when(choreInstanceGenerator.generateInstances(any(Chore.class))).thenReturn(List.of());
        when(userNotificationSettingRepository.findByUserId(anyLong()))
                .thenReturn(Optional.empty());

        when(missionService.increaseMissionCountForAction(eq(user.getId()), eq(
                UserActionType.CREATE_CHORE_WITH_SPACE)))
                .thenReturn(List.of());

        // when
        ChoreDto.ApiResponse<ChoreDto.Response> response = spaceChoreCreator.createChoreFromSpace(1L, anyLong());

        // then
        assertTrue(response.getData().getNotificationYn(), "알림은 켜져 있는 게 Default");
        assertNotNull(response.getData().getNotificationTime(), "알림 시간은 기본값이 정해져 있다.");
        assertEquals(LocalTime.of(19, 0), response.getData().getNotificationTime(), "기본 알림 시각 19시 정각");
    }

    @Test
    @DisplayName("알람 이벤트 발행은 활성화될 시 반드시 이루어진다")
    void create_alarmEvent_whenNotificationYes() {

        Long userId = 1L;
        Long spaceChoreId = 10L;

        // --- 유저, SpaceChore 세팅 ---
        User user = User.builder().id(userId).build();

        SpaceChore spaceChore = SpaceChore.builder()
                .id(spaceChoreId)
                .titleKo("청소하기")
                .space(Space.KITCHEN)
                .repeatType(RepeatType.DAILY)
                .repeatInterval(1)
                .build();

        UserNotificationSetting setting = UserNotificationSetting.createDefault(user, LocalTime.of(19, 0));

        // --- Mock 설정 ---
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(spaceChoreRepository.findById(spaceChoreId)).thenReturn(Optional.of(spaceChore));
        when(userNotificationSettingRepository.findByUserId(userId)).thenReturn(Optional.of(setting));

        // ChoreRepository.save() mock - ID 필수
        Chore savedChore = Chore.builder()
                .id(1L)
                .user(user)
                .title(spaceChore.getTitleKo())
                .space(spaceChore.getSpace())
                .repeatType(spaceChore.getRepeatType())
                .repeatInterval(spaceChore.getRepeatInterval())
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(spaceChore.getRepeatInterval()))
                .notificationYn(setting.isChoreEnabled())
                .notificationTime(setting.getNotificationTime())
                .isDeleted(false)
                .build();

        when(choreRepository.save(any(Chore.class))).thenReturn(savedChore);

        // choreInstanceGenerator.generateInstances() mock - 최소 1개 반환
        ChoreInstance ci = ChoreInstance.builder()
                .chore(savedChore)
                .titleSnapshot(savedChore.getTitle())
                .dueDate(LocalDate.now())
                .build();
        when(choreInstanceGenerator.generateInstances(savedChore))
                .thenReturn(List.of(ci));

        // choreInstanceRepository.saveAll() mock
        when(choreInstanceRepository.saveAll(anyList()))
                .thenAnswer(inv -> inv.getArgument(0));

        // --- 실행 ---
        spaceChoreCreator.createChoreFromSpace(userId, spaceChoreId);

        // --- 이벤트 발행 검증 ---
        verify(applicationEventPublisher, atLeastOnce())
                .publishEvent(any(ChoreInstanceCreatedEvent.class));

        // --- 알람 활성화 여부 검증 ---
        verify(userNotificationSettingRepository, times(1))
                .enableUserNotificationSetting(userId);
    }

}
