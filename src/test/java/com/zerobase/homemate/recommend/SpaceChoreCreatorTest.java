package com.zerobase.homemate.recommend;

import com.zerobase.homemate.badge.service.UserBadgeStatsService;
import com.zerobase.homemate.chore.dto.ChoreDto;
import com.zerobase.homemate.chore.dto.ChoreDto.ApiResponse;
import com.zerobase.homemate.chore.dto.ChoreDto.Response;
import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.SpaceChore;
import com.zerobase.homemate.entity.User;
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
        ApiResponse<Response> response = spaceChoreCreator.createChoreFromSpace(userId, Space.KITCHEN, spaceChoreId);

        // then
        assertEquals("주방 싱크대 정리하기", response.getData().getTitle());
        assertEquals(Space.KITCHEN, response.getData().getSpace());
        verify(choreRepository).save(any(Chore.class));
        verify(choreInstanceRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("이미 등록된 추천 집안일 재등록 시 실패")
    void createChoreFromSpace_shouldFailWhenAlreadyExists() {
        // given
        Long userId = 1L;
        Long spaceChoreId = 1L;

        User user = User.builder().id(userId).build();

        SpaceChore spaceChore = SpaceChore.builder()
                .code("주방")
                .space(Space.KITCHEN)
                .titleKo("주방 설거지")
                .repeatType(RepeatType.DAILY)
                .repeatInterval(1)
                .build();

        // 이미 해당 유저가 동일한 제목의 chore를 가지고 있다고 가정
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(spaceChoreRepository.findById(spaceChoreId)).thenReturn(Optional.of(spaceChore));
        when(choreRepository.existsByUserIdAndTitle(userId, spaceChore.getTitleKo())).thenReturn(true);

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            spaceChoreCreator.createChoreFromSpace(userId, Space.KITCHEN, spaceChoreId);
        });

        // then
        assertEquals(ErrorCode.CHORE_ALREADY_REGISTERED, exception.getErrorCode());
        verify(choreRepository, never()).save(any(Chore.class));
        verify(choreInstanceRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("기본 설정이 없을 때, 알림은 ON 상태이며 시간은 9시로 설정된다.")
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
        ApiResponse<ChoreDto.Response> response = spaceChoreCreator.createChoreFromSpace(1L, Space.KITCHEN, anyLong());

        // then
        assertTrue(response.getData().getNotificationYn(), "알림은 켜져 있는 게 Default");
        assertNotNull(response.getData().getNotificationTime(), "알림 시간은 기본값이 정해져 있다.");
        assertEquals(LocalTime.of(9, 0), response.getData().getNotificationTime(), "기본 알림 시각 9시 정각");
    }
}
