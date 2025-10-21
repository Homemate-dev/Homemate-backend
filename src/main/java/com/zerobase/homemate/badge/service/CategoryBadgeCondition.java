package com.zerobase.homemate.badge.service;

import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.enums.Category;
import com.zerobase.homemate.repository.CategoryChoreRepository;
import com.zerobase.homemate.repository.ChoreRepository;

public class CategoryBadgeCondition implements BadgeCondition {

    private final Category targetCategory;
    private final int requiredCount;
    private final String badgeName;
    private final CategoryChoreRepository categoryChoreRepository;
    private final ChoreRepository choreRepository;

    public CategoryBadgeCondition(Category targetCategory, int requiredCount, String badgeName, CategoryChoreRepository categoryChoreRepository, ChoreRepository choreRepository) {
        this.targetCategory = targetCategory;
        this.requiredCount = requiredCount;
        this.badgeName = badgeName;
        this.categoryChoreRepository = categoryChoreRepository;
        this.choreRepository = choreRepository;
    }

    @Override
    public boolean matchesCondition(Chore chore) {
        // 이 집안일은 특정 카테고리에 속하는지 확인
        boolean belongsCategory =
                categoryChoreRepository.existsByChoreAndCategory(chore, targetCategory);

        // 해당 카테고리의 완료 횟수가 기준 이상인지 확인
        Long completedCount = choreRepository.countByUserAndCategoryAndIsCompletedTrue(chore.getUser(), targetCategory);

        return belongsCategory && completedCount >= requiredCount;
    }

    @Override
    public String getBadgeName() {
        return badgeName;
    }
}
