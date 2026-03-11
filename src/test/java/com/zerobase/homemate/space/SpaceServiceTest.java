package com.zerobase.homemate.space;


import com.zerobase.homemate.entity.SpaceChore;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.enums.RepeatType;
import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.entity.enums.UserRole;
import com.zerobase.homemate.entity.enums.UserStatus;
import com.zerobase.homemate.recommend.dto.ClassifyChoreResponse;
import com.zerobase.homemate.recommend.dto.SpaceResponse;
import com.zerobase.homemate.recommend.service.SpaceService;
import com.zerobase.homemate.repository.ChoreRepository;
import com.zerobase.homemate.repository.SpaceChoreRepository;
import com.zerobase.homemate.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class SpaceServiceTest {
    @Mock
    private SpaceChoreRepository spaceChoreRepository;

    @InjectMocks
    private SpaceService spaceService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChoreRepository choreRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("공간별 집안일 조회 - Repository + RepeatType 우선순위 적용")
    void getChoresBySpace_ShouldReturnSortedByRepeatType() {
        // given

        User user = User.builder()
                        .id(1L)
                        .userRole(UserRole.USER)
                        .userStatus(UserStatus.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .profileName("testUser")
                        .build();


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

        when(userRepository.save(any(User.class))).thenReturn(user);
        when(spaceChoreRepository.findBySpace(eq(Space.KITCHEN)))
                .thenReturn(List.of(chore1, chore2));

        // when
        List<ClassifyChoreResponse> result = spaceService.getSpaceChores(Space.KITCHEN, user.getId());

        // then
        assertThat(result).hasSize(2);
        // RepeatType 우선순위 확인 (DAILY가 WEEKLY보다 먼저)
        assertThat(result.get(0).title()).isEqualTo("설거지");
        assertThat(result.get(1).title()).isEqualTo("청소");

        verify(spaceChoreRepository, times(1))
                .findBySpace(eq(Space.KITCHEN));
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
