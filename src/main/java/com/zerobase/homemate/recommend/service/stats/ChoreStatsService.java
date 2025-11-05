package com.zerobase.homemate.recommend.service.stats;


import com.zerobase.homemate.entity.enums.Category;
import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.mission.service.MissionService;
import com.zerobase.homemate.recommend.dto.TopItemDto;
import com.zerobase.homemate.repository.CategoryChoreRepository;
import com.zerobase.homemate.repository.SpaceChoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ChoreStatsService {

    private final RedisChoreStatsService redisChoreStatsService;
    private final CategoryChoreRepository categoryChoreRepository;
    private final SpaceChoreRepository spaceChoreRepository;
    private final MissionService missionService;

    public List<TopItemDto> getTopOverallWithMissions(Long userId, int topN){

        // 1. Redis 집계 가져오기
        Map<String, Long> categoryCounts = redisChoreStatsService.getCategoryStats();
        Map<String, Long> spaceCounts = redisChoreStatsService.getSpaceStats();

        // 2. Redis TOP N 정렬
        List<String> topOverall = Stream.concat(
                        categoryCounts.entrySet().stream(),
                        spaceCounts.entrySet().stream()
                )
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();

        List<TopItemDto> result = new ArrayList<>();

        // 미션 집안일 수
        Long missionCount = (long) missionService.getMonthlyMissions(userId).size();

        // 3. 미션 카테고리 (Category.MISSIONS)
        result.add(new TopItemDto(Category.MISSIONS.getCategoryName(), Category.MISSIONS.name(), missionCount));

        // 4. 나머지 TOP N
        topOverall.stream()
                .filter(name -> !Category.MISSIONS.name().equals(name))
                .limit(topN)
                .forEach(code -> {
                    String displayName;
                    Long count;
                    try {
                        Category category = Category.valueOf(code);
                        displayName = category.getCategoryName();
                        count = categoryChoreRepository.countByCategory(category);
                    } catch (IllegalArgumentException e1) {
                        try {
                            Space space = Space.valueOf(code);
                            displayName = space.getSpaceName();
                            count = spaceChoreRepository.countBySpace(space);
                        } catch (IllegalArgumentException e2) {
                            displayName = code; // 혹시 Enum이 아닌 경우
                            count = 0L;
                        }
                    }
                    result.add(new TopItemDto(displayName, code, count));
                });

        return result;
    }
}
