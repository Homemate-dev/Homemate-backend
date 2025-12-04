package com.zerobase.homemate.recommend.service;

import com.zerobase.homemate.entity.CategoryChore;
import com.zerobase.homemate.entity.Mission;
import com.zerobase.homemate.entity.SpaceChore;
import com.zerobase.homemate.entity.enums.Category;
import com.zerobase.homemate.entity.enums.RepeatType;
import com.zerobase.homemate.entity.enums.UserActionType;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.mission.service.MissionService;
import com.zerobase.homemate.recommend.dto.CategoryResponse;
import com.zerobase.homemate.recommend.dto.ClassifyChoreResponse;
import com.zerobase.homemate.repository.CategoryChoreRepository;
import com.zerobase.homemate.repository.MissionRepository;
import com.zerobase.homemate.repository.SpaceChoreRepository;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CategoryService {

    private final CategoryChoreRepository categoryChoreRepository;
    private final SpaceChoreRepository spaceChoreRepository;
    private final MissionService missionService;
    private final MissionRepository missionRepository;
    private final int DEFAULT_PAGE_SIZE = 6;

    private static final Map<RepeatType, Integer> REPEAT_PRIORITY = Map.of(
            RepeatType.DAILY, 1,
            RepeatType.WEEKLY, 2,
            RepeatType.MONTHLY, 3,
            RepeatType.YEARLY, 4,
            RepeatType.NONE, 5
    );


    public List<CategoryResponse> getAllCategories() {

        return Arrays.stream(Category.values())
                .map(CategoryResponse::fromEntity)
                .toList();
    }

    public List<ClassifyChoreResponse> getChoresByCategory(Category category){

        if(category == null){
            throw new CustomException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        List<CategoryChore> randomChores = categoryChoreRepository.findByCategory(
                category,
                Pageable.ofSize(DEFAULT_PAGE_SIZE)
        );

        // 조회된 개수 로그 추가
        log.info("getChoresByCategory - category: {}, fetched size: {}",
                category, randomChores.size());

        return randomChores.stream()
                .sorted(Comparator.comparingInt(categoryChore -> REPEAT_PRIORITY.get(categoryChore.getRepeatType())))
                .map(ClassifyChoreResponse::fromCategory)
                .toList();

    }

    @Transactional
    public void updateMonthlyMissionChores() {

        categoryChoreRepository.deleteByCategory(Category.MISSIONS);

        YearMonth yearMonth = YearMonth.now(ZoneId.of("Asia/Seoul"));
        List<Mission> monthlyMissions =
            missionRepository.findByActiveYearMonthAndIsActiveTrueAndUserActionTypeInOrderByIdAsc(
                yearMonth, List.of(UserActionType.COMPLETE_CHORE));

        if (monthlyMissions.isEmpty()) return;

        List<SpaceChore> spaceChores = spaceChoreRepository.findAll();

        if (spaceChores.isEmpty()) return;

        Map<Long, String> spaceTitleCache = spaceChores.stream()
            .collect(Collectors.toMap(SpaceChore::getId, SpaceChore::getTitleKo));

        List<CategoryChore> toSave = new ArrayList<>();

        for (Mission mission : monthlyMissions) {
            final String missionTitle = mission.getTitle();

            for (SpaceChore sc : spaceChores) {
                String choreTitle = spaceTitleCache.get(sc.getId());
                if (!missionService.qualifiesChoreTitle(
                    missionTitle, choreTitle)
                ) continue;

                toSave.add(CategoryChore.builder()
                    .title(choreTitle)
                    .repeatType(sc.getRepeatType())
                    .repeatInterval(sc.getRepeatInterval())
                    .category(Category.MISSIONS)
                    .build());
            }
        }

        if (toSave.isEmpty()) return;
        categoryChoreRepository.saveAll(toSave);
    }
}
