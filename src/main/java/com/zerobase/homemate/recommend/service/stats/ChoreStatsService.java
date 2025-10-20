package com.zerobase.homemate.recommend.service.stats;


import com.zerobase.homemate.entity.enums.Category;
import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.recommend.dto.TopItemDto;
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

    public List<TopItemDto> getTopOverallWithMissions(int topN){

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

        // 미션 달성 집안일 먼저 추가
        result.add(new TopItemDto("미션 달성 집안일", "MISSIONS"));

        topOverall.stream()
                .filter(name -> !"MISSIONS".equals(name))
                .limit(topN)
                .forEach(code -> {
                    String displayName;
                    try {
                        displayName = Category.valueOf(code).getCategoryName();  // Category Enum이면
                    } catch (IllegalArgumentException e1) {
                        try {
                            displayName = Space.valueOf(code).getSpaceName();  // Space Enum이면
                        } catch (IllegalArgumentException e2) {
                            displayName = code; // 혹시 Enum 아닌 경우
                        }
                    }
                    result.add(new TopItemDto(displayName, code));
                });

        return result;
    }
}
