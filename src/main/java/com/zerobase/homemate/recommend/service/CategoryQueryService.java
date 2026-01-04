package com.zerobase.homemate.recommend.service;

import com.zerobase.homemate.entity.Categories;
import com.zerobase.homemate.entity.CategoryChore;
import com.zerobase.homemate.entity.enums.*;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.recommend.dto.CategoryResponse;
import com.zerobase.homemate.recommend.dto.ClassifyChoreResponse;
import com.zerobase.homemate.recommend.dto.SubCategoryResponse;
import com.zerobase.homemate.repository.CategoriesRepository;
import com.zerobase.homemate.repository.CategoryChoreRepository;
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
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CategoryQueryService {

    private final CategoryChoreRepository categoryChoreRepository;
    private final CategoriesRepository categoriesRepository;
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

    public List<ClassifyChoreResponse> getFixedChores(Category category){
        if(category == null){
            throw new CustomException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        List<CategoryChore> fixedCategoryChores = categoryChoreRepository.findActiveFixedByCategory(
                category,
                Pageable.ofSize(DEFAULT_PAGE_SIZE)
        );


        return fixedCategoryChores.stream()
                .sorted(Comparator.comparingInt(categoryChore -> REPEAT_PRIORITY.get(categoryChore.getRepeatType())))
                .map(ClassifyChoreResponse::fromCategory)
                .toList();
    }

    public List<ClassifyChoreResponse> getSeasonChores(Season season){
        if(season == null){
            throw new CustomException(ErrorCode.INVALID_SEASON);
        }

        List<CategoryChore> seasonCategoryChores = categoryChoreRepository.findActiveSeasonalBySeason(
                season,
                Pageable.ofSize(DEFAULT_PAGE_SIZE)
        );

        return seasonCategoryChores.stream()
                .sorted(Comparator.comparingInt(categoryChore -> REPEAT_PRIORITY.get(categoryChore.getRepeatType())))
                .map(ClassifyChoreResponse::fromCategory)
                .toList();
    }


    // 월간 카테고리 조회 시 집안일 리스트 출력
    public List<ClassifyChoreResponse> getMonthlyChores(Long categoriesId, SubCategory subCategory){
        Categories categories = categoriesRepository.findById(categoriesId)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        if (categories.getType() != CategoryType.MONTHLY) {
            throw new CustomException(ErrorCode.INVALID_CATEGORY_TYPE);
        }

        if (!categories.isActive()) {
            throw new CustomException(ErrorCode.INACTIVE_CATEGORY);
        }

        if(subCategory == null){
            List<CategoryChore> chores =
                    categoryChoreRepository.findActiveByCategoriesAndCategoryType(
                            categories,
                            CategoryType.MONTHLY,
                            Pageable.ofSize(DEFAULT_PAGE_SIZE)
                    );

            return chores.stream()
                    .sorted(Comparator.comparingInt(c -> REPEAT_PRIORITY.get(c.getRepeatType())))
                    .map(ClassifyChoreResponse::fromCategory)
                    .toList();
        }

        List<CategoryChore> chores =
                categoryChoreRepository.findActiveByCategoryTypeAndSubCategory(
                        categories,
                        CategoryType.MONTHLY,
                        subCategory,
                        Pageable.ofSize(DEFAULT_PAGE_SIZE)
                );

        return chores.stream()
                .sorted(Comparator.comparingInt(c -> REPEAT_PRIORITY.get(c.getRepeatType())))
                .map(ClassifyChoreResponse::fromCategory)
                .toList();
    }

    // 월간 카테고리들 조회 API
    public List<CategoryResponse> getMonthlyCategories(String targetMonth) {
        return categoriesRepository.findActiveMonthlyByTargetMonth(targetMonth)
                .stream()
                .map(CategoryResponse::fromCategories)
                .toList();
    }

    // Category 필터링 조회 API
    public List<SubCategoryResponse> getSubCategories() {

        return Arrays.stream(SubCategory.values())
                .map(SubCategoryResponse::fromSub)
                .toList();
    }
}
