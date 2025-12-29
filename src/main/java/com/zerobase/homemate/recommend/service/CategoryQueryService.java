package com.zerobase.homemate.recommend.service;

import com.zerobase.homemate.entity.CategoryChore;
import com.zerobase.homemate.entity.enums.Category;
import com.zerobase.homemate.entity.enums.RepeatType;
import com.zerobase.homemate.entity.enums.Season;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.recommend.dto.ClassifyChoreResponse;
import com.zerobase.homemate.repository.CategoryChoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CategoryQueryService {

    private final CategoryChoreRepository categoryChoreRepository;
    private final int DEFAULT_PAGE_SIZE = 6;

    private static final Map<RepeatType, Integer> REPEAT_PRIORITY = Map.of(
            RepeatType.DAILY, 1,
            RepeatType.WEEKLY, 2,
            RepeatType.MONTHLY, 3,
            RepeatType.YEARLY, 4,
            RepeatType.NONE, 5
    );

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

    public List<ClassifyChoreResponse> getMonthlyChores(String yearMonth){
        if(yearMonth == null){
            throw new CustomException(ErrorCode.INVALID_DATE_RANGE);
        }

        List<CategoryChore> monthlyCategoryChores = categoryChoreRepository.findActiveMonthlyByYearMonth(
                yearMonth,
                Pageable.ofSize(DEFAULT_PAGE_SIZE)
        );

        return monthlyCategoryChores.stream()
                .sorted(Comparator.comparingInt(categoryChore -> REPEAT_PRIORITY.get(categoryChore.getRepeatType())))
                .map(ClassifyChoreResponse::fromCategory)
                .toList();
    }
}
