package com.zerobase.homemate.recommend;

import com.zerobase.homemate.badge.service.UserBadgeStatsService;
import com.zerobase.homemate.chore.dto.ChoreDto;
import com.zerobase.homemate.entity.*;
import com.zerobase.homemate.entity.enums.*;
import com.zerobase.homemate.mission.service.MissionService;
import com.zerobase.homemate.notification.component.ChoreInstanceCreatedEvent;
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
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

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
    private ApplicationEventPublisher applicationEventPublisher;

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
                .id(categoryChoreId)
                .category(Category.WINTER)
                .title("청소하기")
                .repeatType(RepeatType.DAILY)
                .repeatInterval(3)
                .build();

        SpaceChore spaceChore = SpaceChore.builder()
                .id(100L)
                .space(Space.KITCHEN)
                .titleKo("청소하기")
                .code("주방")
                .repeatType(RepeatType.DAILY)
                .repeatInterval(3)
                .build();

        // ✔ UserNotificationSetting mock 추가
        UserNotificationSetting defaultSetting =
                UserNotificationSetting.createDefault(user, LocalTime.of(9, 0));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(categoryChoreRepository.findById(categoryChoreId))
                .thenReturn(Optional.of(categoryChore));

        when(spaceChoreRepository.findByTitleKo(categoryChore.getTitle()))
                .thenReturn(Optional.of(spaceChore));

        when(userNotificationSettingRepository.findByUserId(userId))
                .thenReturn(Optional.of(defaultSetting));

        // ✔ choreRepository.save() → ID 부여해서 반환
        when(choreRepository.save(any(Chore.class)))
                .thenAnswer(inv -> {
                    Chore c = inv.getArgument(0);
                    setField(c, "id", 10L);
                    return c;
                });

        // ✔ generateInstances() → 저장된 Chore와 연동된 인스턴스 반환
        when(choreInstanceGenerator.generateInstances(any(Chore.class)))
                .thenAnswer(inv -> {
                    Chore savedChore = inv.getArgument(0);
                    return List.of(
                            ChoreInstance.builder()
                                    .id(1L)
                                    .titleSnapshot(savedChore.getTitle())
                                    .dueDate(LocalDate.now())
                                    .chore(savedChore)
                                    .build()
                    );
                });

        when(missionService.increaseMissionCountForAction(
                eq(userId), eq(UserActionType.CREATE_CHORE_RECOMMENDED)))
                .thenReturn(List.of());

        // when
        ChoreDto.ApiResponse<ChoreDto.Response> response =
                categoryChoreCreator.createChoreFromCategory(userId, categoryChoreId);

        // then
        assertNotNull(response);
        assertNotNull(response.getData());

        ChoreDto.Response data = response.getData();

        // ✔ 핵심 검증 1: 제목 올바르게 설정
        assertEquals("청소하기", data.getTitle());

        // ✔ 핵심 검증 2: Space 매칭 성공
        assertEquals(Space.KITCHEN, data.getSpace());

        // ✔ 핵심 검증 3: Notification 설정 반영 여부
        assertEquals(defaultSetting.isChoreEnabled(), data.getNotificationYn());
        assertEquals(defaultSetting.getNotificationTime(), data.getNotificationTime());

        // ✔ Repository 호출 검증
        verify(choreRepository).save(any(Chore.class));
        verify(choreInstanceRepository).saveAll(anyList());
    }


    @Test
    @DisplayName("notificationTime 기본값 세팅 확인 (UserNotificationSetting 없음 → 19:00 적용)")
    void createChore_shouldSetDefaultNotificationTimeEvenIfDisabled() {
        // --- given ---
        User user = User.builder()
                .id(1L)
                .build();

        CategoryChore template = CategoryChore.builder()
                .title("청소하기")
                .repeatType(RepeatType.DAILY)
                .repeatInterval(1)
                .build();

        // choreRepository.save() 호출 시 ID를 부여하도록 설정
        when(choreRepository.save(any(Chore.class))).thenAnswer(inv -> {
            Chore c = inv.getArgument(0);
            setField(c, "id", 100L);
            return c;
        });

        // generateInstances(saved) → saved를 정확히 참조하는 Instance 반환
        when(choreInstanceGenerator.generateInstances(any(Chore.class))).thenAnswer(inv -> {
            Chore saved = inv.getArgument(0);

            return List.of(
                    ChoreInstance.builder()
                            .id(1L)
                            .chore(saved) // saved 객체 참조(테스트용 chore 아님)
                            .titleSnapshot(saved.getTitle())
                            .dueDate(LocalDate.now())
                            .notificationTime(LocalTime.of(19, 0)) // 기본 알람 시간
                            .choreStatus(ChoreStatus.PENDING)
                            .build()
            );
        });

        // 기본 설정 조회 시 빈 값 → default 생성 로직 타게 함
        when(userNotificationSettingRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(categoryChoreRepository.findById(1L))
                .thenReturn(Optional.of(template));

        when(missionService.increaseMissionCountForAction(eq(1L),
                eq(UserActionType.CREATE_CHORE_RECOMMENDED)))
                .thenReturn(List.of());

        // --- when ---
        ChoreDto.ApiResponse<ChoreDto.Response> response =
                categoryChoreCreator.createChoreFromCategory(1L, 1L);

        // --- then ---
        assertNotNull(response.getData().getNotificationTime(),
                "알림 시간은 기본값으로 세팅되어 있어야 한다");

        assertEquals(LocalTime.of(19, 0), response.getData().getNotificationTime(),
                "기본 알림 시간은 19:00 이어야 한다");

        verify(choreInstanceRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("CategoryChore 기반 집안일 등록 - 중복 Title 있어도 새로운 Chore 생성 + ChoreInstance 생성")
    void createChoreFromCategory_shouldCreateNewChoreAndInstancesEvenIfTitleExists() {

        // given
        Long userId = 1L;
        Long categoryChoreId = 1L;

        User user = User.builder().id(userId).build();

        CategoryChore categoryChore = CategoryChore.builder()
                .id(categoryChoreId)
                .title("청소하기")
                .category(Category.WINTER)
                .repeatType(RepeatType.DAILY)
                .repeatInterval(3)
                .build();

        // 이미 존재하는 SpaceChore (중복 title)
        SpaceChore existingSpaceChore = SpaceChore.builder()
                .id(100L)
                .space(Space.KITCHEN)
                .titleKo("청소하기")
                .build();

        // UserNotificationSetting
        UserNotificationSetting setting = UserNotificationSetting.createDefault(user, LocalTime.of(19, 0));

        // Mocks
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryChoreRepository.findById(categoryChoreId)).thenReturn(Optional.of(categoryChore));
        when(spaceChoreRepository.findByTitleKo(categoryChore.getTitle())).thenReturn(Optional.of(existingSpaceChore));
        when(userNotificationSettingRepository.findByUserId(userId)).thenReturn(Optional.of(setting));

        // choreRepository.save -> id 주입
        when(choreRepository.save(any(Chore.class))).thenAnswer(inv -> {
            Chore c = inv.getArgument(0);
            setField(c, "id", 10L); // private id 세팅
            return c;
        });

        // choreInstanceGenerator.generateInstances -> 새 Chore 기반
        when(choreInstanceGenerator.generateInstances(any(Chore.class))).thenAnswer(inv -> {
            Chore savedChore = inv.getArgument(0);
            return List.of(
                    ChoreInstance.builder()
                            .id(1L)
                            .titleSnapshot(savedChore.getTitle())
                            .dueDate(LocalDate.now())
                            .chore(savedChore)
                            .build()
            );
        });

        when(missionService.increaseMissionCountForAction(eq(userId), eq(UserActionType.CREATE_CHORE_RECOMMENDED)))
                .thenReturn(List.of());

        // when
        ChoreDto.ApiResponse<ChoreDto.Response> response =
                categoryChoreCreator.createChoreFromCategory(userId, categoryChoreId);

        // then
        assertNotNull(response);
        ChoreDto.Response data = response.getData();

        // 새로운 Chore 생성 확인
        assertEquals("청소하기", data.getTitle());
        assertEquals(Space.KITCHEN, data.getSpace());

        // choreRepository.save 호출 확인
        verify(choreRepository, times(1)).save(any(Chore.class));

        // choreInstanceRepository.saveAll 호출 확인
        verify(choreInstanceRepository, times(1)).saveAll(anyList());

        // 새 ChoreInstance와 새 Chore 연결 확인
        // → DTO가 아니라 실제 저장된 Chore 객체 사용
        Chore savedChore = choreRepository.save(
                Chore.builder()
                        .user(user)
                        .title(categoryChore.getTitle())
                        .space(existingSpaceChore.getSpace())
                        .repeatType(categoryChore.getRepeatType())
                        .repeatInterval(categoryChore.getRepeatInterval())
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(categoryChore.getRepeatInterval())) // 예시
                        .notificationYn(setting.isChoreEnabled())
                        .notificationTime(setting.getNotificationTime())
                        .isDeleted(false)
                        .build()
        );

        List<ChoreInstance> savedInstances = choreInstanceGenerator.generateInstances(savedChore);
        assertFalse(savedInstances.isEmpty());
        assertEquals(data.getTitle(), savedInstances.get(0).getTitleSnapshot());
        assertEquals(data.getId(), savedInstances.get(0).getChore().getId());
    }

    @Test
    @DisplayName("알람 이벤트 발행은 활성화될 시, 반드시 이루어진다")
    void create_alarmEvent_whenNotificationYes() {

        Long userId = 1L;
        Long categoryChoreId = 10L;

        // --- 유저, 카테고리 chore, 기존 SpaceChore 세팅 ---
        User user = User.builder().id(userId).build();

        CategoryChore categoryChore = CategoryChore.builder()
                .id(categoryChoreId)
                .title("청소하기")
                .category(Category.WINTER)
                .repeatType(RepeatType.DAILY)
                .repeatInterval(1)
                .build();

        SpaceChore existingSpaceChore = SpaceChore.builder()
                .id(100L)
                .space(Space.KITCHEN)
                .titleKo("청소하기")
                .build();

        UserNotificationSetting setting = UserNotificationSetting.createDefault(user, LocalTime.of(19, 0));

        // --- Mock 설정 ---
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryChoreRepository.findById(categoryChoreId)).thenReturn(Optional.of(categoryChore));
        when(spaceChoreRepository.findByTitleKo(categoryChore.getTitle())).thenReturn(Optional.of(existingSpaceChore));
        when(userNotificationSettingRepository.findByUserId(userId)).thenReturn(Optional.of(setting));

        // ChoreRepository.save() mock - 반드시 ID 세팅
        Chore savedChore = Chore.builder()
                .id(1L) // ID 필수
                .user(user)
                .title(categoryChore.getTitle())
                .space(existingSpaceChore.getSpace())
                .repeatType(categoryChore.getRepeatType())
                .repeatInterval(categoryChore.getRepeatInterval())
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(categoryChore.getRepeatInterval()))
                .notificationYn(setting.isChoreEnabled())
                .notificationTime(setting.getNotificationTime())
                .isDeleted(false)
                .build();

        when(choreRepository.save(any(Chore.class))).thenReturn(savedChore);

        ChoreInstance ci = ChoreInstance.builder()
                .chore(savedChore)
                .titleSnapshot(savedChore.getTitle())
                .dueDate(LocalDate.now())
                .build();


        when(choreInstanceGenerator.generateInstances(savedChore))
                .thenReturn(List.of(ci));


        // categoryChore 기반 집안일 등록 실행
        categoryChoreCreator.createChoreFromCategory(userId, categoryChoreId);

        // 알람 이벤트 발행 검증
        verify(applicationEventPublisher, atLeastOnce())
                .publishEvent(any(ChoreInstanceCreatedEvent.class));
    }


}
