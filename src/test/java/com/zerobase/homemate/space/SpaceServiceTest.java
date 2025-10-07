package com.zerobase.homemate.space;


import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.enums.RepeatType;
import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.recommend.dto.ChoreResponse;
import com.zerobase.homemate.recommend.service.SpaceService;
import com.zerobase.homemate.repository.ChoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class SpaceServiceTest {
    @Mock
    private ChoreRepository choreRepository;

    @InjectMocks
    private SpaceService spaceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetChoresBySpace() {
        // given
        Chore chore1 = Chore.builder()
                .id(1L)
                .title("청소")
                .repeatType(RepeatType.WEEKLY)
                .space(Space.KITCHEN)
                .isDeleted(false)
                .build();

        Chore chore2 = Chore.builder()
                .id(2L)
                .title("설거지")
                .repeatType(RepeatType.DAILY)
                .space(Space.KITCHEN)
                .isDeleted(false)
                .build();

        when(choreRepository.findBySpaceAndIsDeletedFalse(Space.KITCHEN))
                .thenReturn(List.of(chore1, chore2));

        // when
        List<ChoreResponse> result = spaceService.getChoresBySpace(Space.KITCHEN);

        // then
        assertEquals(2, result.size());
        assertEquals("청소", result.get(0).title());
        assertEquals("설거지", result.get(1).title());

        verify(choreRepository, times(1)).findBySpaceAndIsDeletedFalse(Space.KITCHEN);
    }

    @Test
    void testGetAllSpaces() {
        var result = spaceService.getAllSpaces();
        assertEquals(Space.values().length, result.size());
        assertEquals("KITCHEN", result.get(0).get("name"));
        assertEquals("주방", result.get(0).get("description"));
    }
}
