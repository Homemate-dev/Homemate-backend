package com.zerobase.homemate.recommend;

import com.zerobase.homemate.entity.enums.Category;
import com.zerobase.homemate.mission.service.MissionService;
import com.zerobase.homemate.recommend.dto.TopItemDto;
import com.zerobase.homemate.recommend.service.stats.ChoreStatsService;
import com.zerobase.homemate.recommend.service.stats.RedisChoreStatsService;
import com.zerobase.homemate.repository.CategoryChoreRepository;
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
    private CategoryChoreRepository categoryChoreRepository;
    private MissionService missionService;

    @BeforeEach
    void setUp() {
        redisChoreStatsService = mock(RedisChoreStatsService.class);
        categoryChoreRepository = mock(CategoryChoreRepository.class);
        missionService = mock(MissionService.class);

        choreStatsService = new ChoreStatsService(
                redisChoreStatsService,
                categoryChoreRepository,
                missionService
        );
    }

    @Test
    @DisplayName("미션 달성 집안일 우선순위 조회 + Top N 조회")
    void testGetTopOverallWithMissions() {
        // given
        Long userId = 1L;

        // Redis mock 데이터
        Map<String, Long> categoryCounts = Map.of(
                "WINTER", 4L,
                "SAFETY_CHECK", 6L,
                "WEEKEND_WHOLE_ROUTINE", 3L,
                "TEN_MINUTES_CLEANING", 5L,
                "APPLIANCE_MAINTENANCE", 2L,
                "HOTEL_BATHROOM", 1L
        );

        when(redisChoreStatsService.getCategoryStats()).thenReturn(categoryCounts);

        // when
        List<TopItemDto> result = choreStatsService.getTopOverallWithMissions(userId, 5);

        // then
        assertEquals(6, result.size()); // 미션 + Top5

        // 첫 번째는 항상 미션
        assertEquals("미션 달성 집안일", result.get(0).name());
        assertEquals(Category.MISSIONS, result.get(0).category());

        // TopN 순서 검증 (미션 제외)
        List<Category> expectedCategories = List.of(
                Category.SAFETY_CHECK,  // 6
                Category.TEN_MINUTES_CLEANING,   // 5
                Category.WINTER,   // 4
                Category.WEEKEND_WHOLE_ROUTINE,   // 3
                Category.APPLIANCE_MAINTENANCE    // 2
        );

        for (int i = 0; i < expectedCategories.size(); i++) {
            assertEquals(expectedCategories.get(i), result.get(i + 1).category());
        }
    }


}
