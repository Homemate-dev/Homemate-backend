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
import com.zerobase.homemate.repository.ChoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.YearMonth;
import java.util.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CategoryQueryService {

    private final CategoryChoreRepository categoryChoreRepository;
    private final CategoriesRepository categoriesRepository;
    private final ChoreRepository choreRepository;
    private final int DEFAULT_LIMIT_SIZE = 6;

    private static final Map<RepeatType, Integer> REPEAT_PRIORITY = Map.of(
            RepeatType.DAILY, 1,
            RepeatType.WEEKLY, 2,
            RepeatType.MONTHLY, 3,
            RepeatType.YEARLY, 4,
            RepeatType.NONE, 5
    );

    public List<CategoryResponse> getAllCategories() {

        String targetMonth = YearMonth.now().toString();

        // 1. 기본 카테고리 (Enum)
        List<CategoryResponse> result = new ArrayList<>(Arrays.stream(Category.values())
                .map(CategoryResponse::fromEntity)
                .toList());

        // 2. 월간 카테고리 (존재하면만 포함)
        List<CategoryResponse> monthly = categoriesRepository
                .findActiveMonthlyByTargetMonth(targetMonth)
                .stream()
                .map(CategoryResponse::fromCategories)
                .toList();


        if (!monthly.isEmpty()) {
            result.addAll(monthly);
        }

        return result;
    }

    // 고정 카테고리 조회
    public List<ClassifyChoreResponse> getFixedChores(Category category, Long userId){
        if(category == null){
            throw new CustomException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        List<CategoryChore> fixedCategoryChores = new ArrayList<>(
                categoryChoreRepository.findActiveFixedByCategory(category)
        );

        Collections.shuffle(fixedCategoryChores);


        // 사용자 Chore Title 조회
        Set<String> userChoreTitles = getUserSystemChoreTitles(userId);



        return mapWithDuplicateFlags(fixedCategoryChores.stream()
                .limit(DEFAULT_LIMIT_SIZE).toList(), userChoreTitles);
    }

    // 계절별 집안일 조회
    public List<ClassifyChoreResponse> getSeasonChores(Season season, Long userId){
        if(season == null){
            throw new CustomException(ErrorCode.INVALID_SEASON);
        }



        List<CategoryChore> seasonCategoryChores = new ArrayList<>(
                categoryChoreRepository.findActiveSeasonalBySeason(season)
        );

        Set<String> userChoreTitles = getUserSystemChoreTitles(userId);

        Collections.shuffle(seasonCategoryChores);
        return mapWithDuplicateFlags(seasonCategoryChores.stream()
                .limit(DEFAULT_LIMIT_SIZE).toList(), userChoreTitles);
    }

    // 월간 카테고리 조회
    public List<ClassifyChoreResponse> getMonthlyChores(
            Long categoriesId, SubCategory subCategory, Long userId) {

        Categories categories = categoriesRepository.findById(categoriesId)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        log.info("[CHORE] categoryId={}, active={}",
                categories.getId(),
                categories.isActive());

        if (categories.getType() != CategoryType.MONTHLY) {
            throw new CustomException(ErrorCode.INVALID_CATEGORY_TYPE);
        }

        if (!categories.isActive()) {
            throw new CustomException(ErrorCode.INACTIVE_CATEGORY);
        }

        Set<String> userChoreTitles = getUserSystemChoreTitles(userId);

        List<CategoryChore> chores = new ArrayList<>(
                subCategory == null
                        ? categoryChoreRepository.findActiveByCategoriesAndCategoryType(
                        categories, CategoryType.MONTHLY)
                        : categoryChoreRepository.findActiveByCategoryTypeAndSubCategory(
                        categories, CategoryType.MONTHLY, subCategory)
        );

        log.info("[CHORE] fetched chores size={}", chores.size());
        chores.forEach(c -> log.info("[CHORE] id={}, title={}, active={}, type={}",
                c.getId(),
                c.getTitle(),
                c.isActive(),
                c.getCategoryType()));

        return mapWithDuplicateFlags(
                chores.stream().toList(),
                userChoreTitles
        );
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

    // Submethod - Duplicate Flag 매기는 Method
    private List<ClassifyChoreResponse> mapWithDuplicateFlags(
            List<CategoryChore> chores,
            Set<String> userChoreTitles
    ){
        return chores.stream()
                .sorted(Comparator.comparingInt(
                        c -> REPEAT_PRIORITY.get(c.getRepeatType())
                ))
                .map(c -> ClassifyChoreResponse.fromCategory(
                        c,
                        userChoreTitles.contains(c.getTitle())
                ))
                .toList();
    }

    // Submethod - 중복되는 집안일 담는 Set 반환
    private Set<String> getUserSystemChoreTitles(Long userId) {
        return new HashSet<>(
                choreRepository.findActiveTitlesByUserIdAndRegistrationTypes(
                        userId,
                        List.of(RegistrationType.CATEGORY, RegistrationType.SPACE)
                )
        );
    }
}
