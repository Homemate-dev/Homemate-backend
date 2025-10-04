package com.zerobase.homemate.space;

import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.Space;
import com.zerobase.homemate.entity.SpaceChore;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.enums.UserRole;
import com.zerobase.homemate.entity.enums.UserStatus;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.recommend.dto.SpaceChoreResponse;
import com.zerobase.homemate.recommend.dto.SpaceResponse;
import com.zerobase.homemate.recommend.service.SpaceService;
import com.zerobase.homemate.repository.ChoreRepository;
import com.zerobase.homemate.repository.SpaceChoreRepository;
import com.zerobase.homemate.repository.SpaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.zerobase.homemate.entity.enums.RepeatType.WEEKLY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class SpaceServiceTest {

    @Mock
    private SpaceRepository spaceRepository;

    @Mock
    private SpaceChoreRepository spaceChoreRepository;

    @Mock
    private ChoreRepository choreRepository;

    @InjectMocks
    private SpaceService spaceService;

    private Space space;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        space = Space.builder()
                .id(1L)
                .code("LIVING")
                .nameKo("거실")
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("공간 전체 조회 성공")
    void testGetAllSpaces_Success() {
        when(spaceRepository.findAll()).thenReturn(List.of(space));

        List<SpaceResponse> result = spaceService.getAllSpaces();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("거실");
        verify(spaceRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("공간별 랜덤 집안일 추천 성공")
    void testGetRandomChoresBySpace_Success() {
        // given
        User user = User.builder()
                .id(1L)
                .userRole(UserRole.USER)
                .userStatus(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();


        Space space = Space.builder()
                .id(1L)
                .code("LIVING_ROOM")
                .nameKo("거실")
                .isActive(true)
                .build();

        Chore chore1 = Chore.builder()
                .id(1L)
                .title("청소기 돌리기")
                .isDeleted(false)
                .user(user)
                .notificationYn(true)
                .notificationTime(LocalTime.MIN)
                .repeatType(WEEKLY)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .build();

        Chore chore2 = Chore.builder()
                .id(2L)
                .title("창문 닦기")
                .isDeleted(false)
                .user(user)
                .notificationYn(true)
                .notificationTime(LocalTime.MIN)
                .repeatType(WEEKLY)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .build();

        SpaceChore sc1 = SpaceChore.builder()
                .id(1L)
                .space(space)
                .chore(chore1)
                .isActive(true)
                .defaultFreq(WEEKLY)
                .build();

        SpaceChore sc2 = SpaceChore.builder()
                .id(2L)
                .space(space)
                .chore(chore2)
                .isActive(true)
                .defaultFreq(WEEKLY)
                .build();
        when(spaceRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(space));
        when(spaceChoreRepository.findBySpaceAndIsActiveTrue(space)).thenReturn(Arrays.asList(sc1, sc2));

        List<SpaceChoreResponse> result = spaceService.getRandomChoresBySpace(1L);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).choreTitle()).isIn("청소기 돌리기", "창문 닦기");
        verify(spaceRepository).findByIdAndIsActiveTrue(1L);
        verify(spaceChoreRepository).findBySpaceAndIsActiveTrue(space);
    }

    @Test
    @DisplayName("공간 ID가 존재하지 않으면 예외 발생")
    void testGetRandomChoresBySpace_NotFound() {
        when(spaceRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> spaceService.getRandomChoresBySpace(1L))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.SPACE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("활성화된 집안일이 없을 때 빈 리스트 반환")
    void testGetRandomChoresBySpace_Empty() {
        when(spaceRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(space));
        when(spaceChoreRepository.findBySpaceAndIsActiveTrue(space)).thenReturn(Collections.emptyList());

        List<SpaceChoreResponse> result = spaceService.getRandomChoresBySpace(1L);

        assertThat(result).isEmpty();
    }
}
