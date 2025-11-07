package com.zerobase.homemate.space;


import com.zerobase.homemate.entity.SpaceChore;
import com.zerobase.homemate.entity.enums.RepeatType;
import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.recommend.dto.ClassifyChoreResponse;
import com.zerobase.homemate.recommend.dto.SpaceResponse;
import com.zerobase.homemate.recommend.service.SpaceService;
import com.zerobase.homemate.repository.SpaceChoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class SpaceServiceTest {
    @Mock
    private SpaceChoreRepository spaceChoreRepository;

    @InjectMocks
    private SpaceService spaceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("공간별 집안일 조회 - Repository + RepeatType 우선순위 적용")
    void getChoresBySpace_ShouldReturnSortedByRepeatType() {
        // given
        SpaceChore chore1 = SpaceChore.builder()
                .id(1L)
                .titleKo("청소")
                .repeatType(RepeatType.WEEKLY)
                .repeatInterval(2)
                .space(Space.KITCHEN)
                .build();

        SpaceChore chore2 = SpaceChore.builder()
                .id(2L)
                .titleKo("설거지")
                .repeatType(RepeatType.DAILY)
                .repeatInterval(1)
                .space(Space.KITCHEN)
                .build();

        when(spaceChoreRepository.findBySpace(eq(Space.KITCHEN)))
                .thenReturn(List.of(chore1, chore2));

        // when
        List<ClassifyChoreResponse> result = spaceService.getChoresBySpace(Space.KITCHEN);

        // then
        assertThat(result).hasSize(2);
        // RepeatType 우선순위 확인 (DAILY가 WEEKLY보다 먼저)
        assertThat(result.get(0).title()).isEqualTo("설거지");
        assertThat(result.get(1).title()).isEqualTo("청소");

        verify(spaceChoreRepository, times(1))
                .findBySpace(eq(Space.KITCHEN));
    }

    @Test
    @DisplayName("공간이 null일 경우 SPACE_NOT_FOUND 예외 발생")
    void getChoresBySpace_ShouldThrowExceptionIfSpaceNull() {
        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> spaceService.getChoresBySpace(null));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SPACE_NOT_FOUND);
    }

    @Test
    @DisplayName("모든 공간 정보 조회 - Enum 기반으로 SpaceResponse 리스트 반환")
    void getAllSpaces_ShouldReturnAllSpaceResponses() {
        // when
        List<SpaceResponse> result = spaceService.getAllSpaces();

        // then
        assertThat(result).isNotEmpty();
        assertThat(result)
                .extracting(SpaceResponse::spaceName)
                .containsExactlyInAnyOrder(
                        Arrays.stream(Space.values())
                                .map(Enum::name)
                                .toArray(String[]::new)
                );
        assertThat(result)
                .allMatch(r -> r.space() != null);
    }
}
