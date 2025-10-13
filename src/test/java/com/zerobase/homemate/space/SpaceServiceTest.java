package com.zerobase.homemate.space;


import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.SpaceChore;
import com.zerobase.homemate.entity.enums.RepeatType;
import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.recommend.dto.ChoreResponse;
import com.zerobase.homemate.recommend.service.SpaceService;
import com.zerobase.homemate.repository.ChoreRepository;
import com.zerobase.homemate.repository.SpaceChoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class SpaceServiceTest {

    @Mock
    private ChoreRepository choreRepository;

    @Mock
    private SpaceChoreRepository spaceChoreRepository;

    @InjectMocks
    private SpaceService spaceService;

    private SpaceChore kitchenSpaceChore;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        // given : SpaceChore(주방)

        kitchenSpaceChore = SpaceChore.builder()
                .id(1L)
                .code("KITCHEN_DEFAULT")
                .titleKo("주방")
                .isActive(true)
                .defaultFreq(RepeatType.DAILY)
                .space(Space.KITCHEN)
                .build();
    }

    @Test
    void testGetChoresBySpace() {
        // given: Chore 두 개 생성
        Chore chore1 = Chore.builder()
                .id(1L)
                .title("청소")
                .repeatType(RepeatType.WEEKLY)
                .spaceChore(kitchenSpaceChore)
                .isDeleted(false)
                .build();

        Chore chore2 = Chore.builder()
                .id(2L)
                .title("설거지")
                .repeatType(RepeatType.DAILY)
                .spaceChore(kitchenSpaceChore)
                .isDeleted(false)
                .build();

        when(spaceChoreRepository.findBySpace(Space.KITCHEN))
                .thenReturn(Optional.of(kitchenSpaceChore));

        when(choreRepository.findBySpaceChoreAndIsDeletedFalse(kitchenSpaceChore))
                .thenReturn(List.of(chore1, chore2));

        // when
        List<ChoreResponse> result = spaceService.getChoresBySpace(Space.KITCHEN);

        // then
        assertEquals(2, result.size());
        assertEquals("청소", result.get(0).title());
        assertEquals("설거지", result.get(1).title());

        verify(spaceChoreRepository, times(1)).findBySpace(Space.KITCHEN);
        verify(choreRepository, times(1)).findBySpaceChoreAndIsDeletedFalse(kitchenSpaceChore);
    }

    @Test
    void testGetAllSpaces() {
        var result = spaceService.getAllSpaces();

        assertEquals(Space.values().length, result.size());
        assertEquals("KITCHEN", result.get(0).get("name"));
        assertEquals("주방", result.get(0).get("description"));
    }
}
