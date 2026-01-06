package com.zerobase.homemate.recommend.service;

import com.zerobase.homemate.entity.CategoryChore;
import com.zerobase.homemate.entity.Mission;
import com.zerobase.homemate.entity.SpaceChore;
import com.zerobase.homemate.entity.enums.Category;
import com.zerobase.homemate.entity.enums.CategoryType;
import com.zerobase.homemate.entity.enums.UserActionType;
import com.zerobase.homemate.mission.service.MissionService;
import com.zerobase.homemate.repository.CategoryChoreRepository;
import com.zerobase.homemate.repository.MissionRepository;
import com.zerobase.homemate.repository.SpaceChoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MonthlyCategoryService {

    private final CategoryChoreRepository categoryChoreRepository;
    private final SpaceChoreRepository spaceChoreRepository;
    private final MissionService missionService;
    private final MissionRepository missionRepository;

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

    @Transactional
    public void refreshMonthlyCategories() {
        YearMonth current = YearMonth.now(ZoneId.of("Asia/Seoul"));
        YearMonth previous = current.minusMonths(1);

        deactivate(previous);
        activate(current);
    }

    private void deactivate(YearMonth yearMonth) {
        categoryChoreRepository.deactivateAllMonthly(
                CategoryType.MONTHLY,
                yearMonth.toString()
        );
    }

    private void activate(YearMonth yearMonth) {
        categoryChoreRepository.activateMonthly(
                CategoryType.MONTHLY,
                yearMonth.toString()
        );
    }
}
