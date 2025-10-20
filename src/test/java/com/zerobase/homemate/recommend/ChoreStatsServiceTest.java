package com.zerobase.homemate.recommend;

import com.zerobase.homemate.recommend.dto.TopItemDto;
import com.zerobase.homemate.recommend.service.stats.ChoreStatsService;
import com.zerobase.homemate.recommend.service.stats.RedisChoreStatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChoreStatsServiceTest {

    private RedisChoreStatsService redisChoreStatsService;
    private ChoreStatsService choreStatsService;

    @BeforeEach
    void setUp() {
        redisChoreStatsService = mock(RedisChoreStatsService.class);
        choreStatsService = new ChoreStatsService(redisChoreStatsService);
    }

    @Test
    @DisplayName("미션 달성 집안일 우선순위 조회 + Top N 조회")
    void testGetTopOverallWithMissions() {
        // given: Redis에 저장된 값 모킹
        Map<String, Long> categoryCounts = Map.of(
                "WINTER", 4L,
                "FIFTEEN", 6L,
                "SPRING", 3L,
                "SUMMER", 5L,
                "AUTUMN", 2L,
                "EXTRA", 1L
        );
        Map<String, Long> spaceCounts = Map.of(
                "ETC", 8L,
                "KITCHEN", 2L
        );

        when(redisChoreStatsService.getCategoryStats()).thenReturn(categoryCounts);
        when(redisChoreStatsService.getSpaceStats()).thenReturn(spaceCounts);

        // when
        List<TopItemDto> result = choreStatsService.getTopOverallWithMissions(5); // TopN = 5

        // then
        assertEquals(6, result.size()); // 총 5 + 1개 반환

        // 첫 번째는 항상 미션
        assertEquals("미션 달성 집안일", result.get(0).name());
        assertEquals("MISSIONS", result.get(0).code());

        // TopN 순서 검증 (미션 제외하고 상위 5개)
        List<String> expectedCodes = List.of("ETC", "FIFTEEN", "SUMMER", "WINTER", "SPRING");
        for (int i = 0; i < expectedCodes.size(); i++) {
            assertEquals(expectedCodes.get(i), result.get(i + 1).code());
        }
    }

}
