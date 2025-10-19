package com.zerobase.homemate.recommend;

import com.zerobase.homemate.recommend.dto.SpaceChoreResponse;
import com.zerobase.homemate.recommend.service.RecommendService;
import com.zerobase.homemate.repository.SpaceChoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class RecommendServiceTest {

    @Mock
    private SpaceChoreRepository spaceChoreRepository;

    @InjectMocks
    private RecommendService recommendService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getRandomChores_shouldReturnThreeChores(){
        // given - 익명 클래스 방식으로 두 필드 구현
        SpaceChoreResponse sc1 = new SpaceChoreResponse() {
            public Long getId() { return 1L; }
            public String getTitleKo() { return "주방 싱크대 청소하기"; }
        };
        SpaceChoreResponse sc2 = new SpaceChoreResponse() {
            public Long getId() { return 2L; }
            public String getTitleKo() { return "신발정리"; }
        };
        SpaceChoreResponse sc3 = new SpaceChoreResponse() {
            public Long getId() { return 3L; }
            public String getTitleKo() { return "환기하기"; }
        };

        SpaceChoreResponse sc4 = new SpaceChoreResponse() {
            public Long getId() { return 4L; }
            public String getTitleKo() { return "에어컨 필터 청소하기"; }
        };

        when(spaceChoreRepository.findRandomChores()).thenReturn(List.of(sc1, sc2, sc4));

        List<SpaceChoreResponse> responses = recommendService.getRandomChores();

        assertEquals(3, responses.size());
        assertEquals("신발정리", responses.get(1).getTitleKo());
    }
}
