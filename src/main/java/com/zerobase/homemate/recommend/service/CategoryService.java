package com.zerobase.homemate.recommend.service;

import com.zerobase.homemate.entity.Category;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.recommend.dto.CategoryResponse;
import com.zerobase.homemate.recommend.dto.ChoreResponse;
import com.zerobase.homemate.repository.CategoryRepository;
import com.zerobase.homemate.repository.ChoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final ChoreRepository choreRepository;
    private final CategoryRepository categoryRepository;
    private static final int DEFAULT_PAGE_SIZE = 4;

    public List<ChoreResponse> getChoresByCategory(Long categoryId) {
        // 카테고리 존재 여부 확인
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        // 집안일 조회 (없으면 빈 리스트 반환)
        return choreRepository.findByCategoryChores_Category_Id(
                        categoryId,
                        PageRequest.of(0, DEFAULT_PAGE_SIZE)
                )
                .stream()
                .map(ChoreResponse::fromEntity)
                .toList();
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(CategoryResponse::fromEntity)
                .toList();
    }


}
