package com.zerobase.homemate.recommend.service.stats;


import com.zerobase.homemate.entity.enums.Category;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.mission.service.MissionService;
import com.zerobase.homemate.recommend.dto.TopItemDto;
import com.zerobase.homemate.repository.CategoryChoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChoreStatsService {

    private final RedisChoreStatsService redisChoreStatsService;
    private final CategoryChoreRepository categoryChoreRepository;
    private final MissionService missionService;

    public List<TopItemDto> getTopOverallWithMissions(Long userId, int topN){

        // 1. Redis 집계 가져오기
        Map<String, Long> categoryCounts = redisChoreStatsService.getCategoryStats();

        // 2. Redis TOP N 정렬
        List<String> topOverall = categoryCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();

        List<TopItemDto> result = new ArrayList<>();

        // 미션 집안일 수
        Long missionCount = (long) missionService.getMonthlyMissions(userId).size();

        // 3. 미션 카테고리 (Category.MISSIONS)
        result.add(new TopItemDto(Category.MISSIONS.getCategoryName(), Category.MISSIONS, missionCount));

        // 4. 나머지 TOP N
        topOverall.stream()
                .filter(name -> !Category.MISSIONS.name().equals(name))
                .limit(topN)
                .forEach(code -> {
                    try {
                        Category category = Category.valueOf(code);
                        String displayName = category.getCategoryName();
                        Long count = categoryChoreRepository.countByCategory(category);
                        result.add(new TopItemDto(displayName, category, count));
                    } catch (IllegalArgumentException e) {

                        throw new CustomException(ErrorCode.CATEGORY_NOT_FOUND);
                    }
                });

        return result;
    }
}
