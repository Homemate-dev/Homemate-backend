package com.zerobase.homemate.space;

import com.zerobase.homemate.entity.Space;
import com.zerobase.homemate.entity.SpaceChore;
import com.zerobase.homemate.recommend.service.SpaceService;
import com.zerobase.homemate.repository.ChoreRepository;
import com.zerobase.homemate.repository.SpaceChoreRepository;
import com.zerobase.homemate.repository.SpaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private SpaceChore spaceChore;
    private SpaceChore spaceChore2;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);

        space = Space.builder()
                .id(1L)
                .nameKo("거실")
                .code("LIVING_ROOM")
                .isActive(true)
                .build();

        spaceChore = SpaceChore.builder()
                .id(1L)
                .titleKo("거실 청소")
                .space(space)
                .isActive(true)
                .build();

        spaceChore2 = SpaceChore.builder()
                .id(2L)
                .titleKo("정리")
                .space(space)
                .isActive(true)
                .build();

    }

    @Test
    void testGetAllChoresBySpace(){

        when(spaceRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(space));
        when(spaceChoreRepository.findBySpaceAndIsActiveTrue(space)).thenReturn(Arrays.asList(spaceChore, spaceChore2));

        List<SpaceChore> list = spaceService.getAllChoresBySpace(1L);

        assertThat(list).hasSize(2).contains(spaceChore, spaceChore2);
        verify(spaceRepository).findByIdAndIsActiveTrue(1L);
        verify(spaceChoreRepository).findBySpaceAndIsActiveTrue(space);

    }

    @Test
    void testGetRandomChoresBySpace(){
        when(spaceRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(space));
        when(spaceChoreRepository.findBySpaceAndIsActiveTrue(space)).thenReturn(Arrays.asList(spaceChore));

        List<SpaceChore> result = spaceService.getRandomChoresBySpace(1L);

        assertThat(result).hasSize(1);
        assertThat(Arrays.asList(spaceChore, spaceChore2)).contains(result.get(0));
    }
}
