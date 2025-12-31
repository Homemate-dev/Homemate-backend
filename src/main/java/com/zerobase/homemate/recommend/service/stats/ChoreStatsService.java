package com.zerobase.homemate.recommend.service.stats;


import com.zerobase.homemate.entity.Categories;
import com.zerobase.homemate.entity.Mission;
import com.zerobase.homemate.entity.enums.Category;
import com.zerobase.homemate.entity.enums.CategoryType;
import com.zerobase.homemate.entity.enums.MissionType;
import com.zerobase.homemate.entity.enums.Season;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.recommend.dto.TopItemDto;
import com.zerobase.homemate.repository.CategoriesRepository;
import com.zerobase.homemate.repository.CategoryChoreRepository;
import com.zerobase.homemate.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChoreStatsService {

    private final RedisChoreStatsService redisChoreStatsService;
    private final CategoryChoreRepository categoryChoreRepository;
    private final MissionRepository missionRepository;
    private final CategoriesRepository categoriesRepository;

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

    public List<TopItemDto> getTopCategories(Long userId){
        List<TopItemDto> result = new ArrayList<>();

        result.add(getMissionTop());
        result.add(getSeasonTop());
        result.add(getRandomMonthlyTop());

        return result;
    }

    private TopItemDto getMissionTop() {
        List<Mission> monthlyMissions =
                missionRepository.findByActiveYearMonthAndIsActiveTrueOrderByIdAsc(YearMonth.now());

        long count = monthlyMissions.stream()
                .filter(m -> CHORE_MISSION_TYPES.contains(m.getMissionType()))
                .count();

        return new TopItemDto(
                Category.MISSIONS.getCategoryName(),
                Category.MISSIONS,
                count
        );
    }

    private TopItemDto getSeasonTop() {
        Season currentSeason = Season.from(LocalDate.now(ZoneId.of("Asia/Seoul")));

        long count = categoryChoreRepository.countBySeasonAndCategoryType(
                currentSeason,
                CategoryType.SEASON
        );

        return new TopItemDto(
                currentSeason.name(),
                null,
                count
        );
    }

    private TopItemDto getRandomMonthlyTop() {
        List<Categories> monthlyCategories =
                categoriesRepository.findActiveMonthlyByYearMonth(YearMonth.now().toString());

        if (monthlyCategories.isEmpty()) {
            throw new CustomException(ErrorCode.ACTIVE_CATEGORY_NOT_FOUND);
        }

        Categories selected =
                monthlyCategories.get(
                        ThreadLocalRandom.current().nextInt(monthlyCategories.size())
                );

        long count = categoryChoreRepository.countByCategories(selected);

        return new TopItemDto(
                selected.getTitle(),
                null,
                count
        );
    }


    private static final Set<MissionType> CHORE_MISSION_TYPES = Set.of(
            MissionType.CHORE,
            MissionType.MONTHLY_CHORE
    );
}
