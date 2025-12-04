package com.zerobase.homemate.recommend.service.stats;


import com.zerobase.homemate.entity.Mission;
import com.zerobase.homemate.entity.enums.Category;
import com.zerobase.homemate.entity.enums.MissionType;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.recommend.dto.TopItemDto;
import com.zerobase.homemate.repository.CategoryChoreRepository;
import com.zerobase.homemate.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChoreStatsService {

    private final RedisChoreStatsService redisChoreStatsService;
    private final CategoryChoreRepository categoryChoreRepository;
    private final MissionRepository missionRepository;

    public List<TopItemDto> getTopOverallWithMissions(Long userId){

        // 1. Redis 집계 가져오기
        Map<String, Long> categoryCounts = redisChoreStatsService.getCategoryStats();

        // 2. Redis TOP N 정렬
        List<String> topOverall = categoryCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();

        List<TopItemDto> result = new ArrayList<>();

        List<Mission> monthlyMissions =
                missionRepository.findByActiveYearMonthAndIsActiveTrueOrderByIdAsc(YearMonth.now());


        // 미션 집안일 수
        Long missionCount = monthlyMissions.stream()
                .filter(mission -> CHORE_MISSION_TYPES.contains(mission.getMissionType()))
                .count();


        // 3. 미션 카테고리 (Category.MISSIONS)
        result.add(new TopItemDto(Category.MISSIONS.getCategoryName(), Category.MISSIONS, missionCount));

        log.info("Mission Category Added : {}", missionCount);


        // 4. 나머지 TOP N
        topOverall.stream()
                .filter(name -> !Category.MISSIONS.name().equals(name))
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

    private static final Set<MissionType> CHORE_MISSION_TYPES = Set.of(
            MissionType.CHORE,
            MissionType.MONTHLY_CHORE
    );
}
