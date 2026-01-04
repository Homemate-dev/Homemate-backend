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
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChoreStatsService {

    private final CategoryChoreRepository categoryChoreRepository;
    private final MissionRepository missionRepository;
    private final CategoriesRepository categoriesRepository;

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
                categoriesRepository.findActiveMonthlyByTargetMonth(YearMonth.now().toString());

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
