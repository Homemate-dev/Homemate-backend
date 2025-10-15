package com.zerobase.homemate.recommend.service;

import com.zerobase.homemate.entity.CategoryChore;
import com.zerobase.homemate.entity.enums.Category;
import com.zerobase.homemate.entity.enums.RepeatType;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.recommend.dto.CategoryResponse;
import com.zerobase.homemate.recommend.dto.ClassifyChoreResponse;
import com.zerobase.homemate.repository.CategoryChoreRepository;
import lombok.RequiredArgsConstructor;
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
public class CategoryService {

    private final CategoryChoreRepository categoryChoreRepository;
    private final int DEFAULT_PAGE_SIZE = 5;

    private static final Map<RepeatType, Integer> REPEAT_PRIORITY = Map.of(
            RepeatType.DAILY, 1,
            RepeatType.WEEKLY, 2,
            RepeatType.MONTHLY, 3,
            RepeatType.NONE, 4
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

        return randomChores.stream()
                .sorted(Comparator.comparingInt(categoryChore -> REPEAT_PRIORITY.get(categoryChore.getRepeatType())))
                .map(ClassifyChoreResponse::fromCategory)
                .toList();

    }


}
