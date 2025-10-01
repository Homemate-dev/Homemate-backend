package com.zerobase.homemate.recommend.service;




import com.zerobase.homemate.entity.Category;
import com.zerobase.homemate.entity.CategoryChore;
import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.recommend.dto.ChoreResponse;
import com.zerobase.homemate.repository.CategoryChoreRepository;
import com.zerobase.homemate.repository.CategoryRepository;
import com.zerobase.homemate.repository.ChoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;



@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final ChoreRepository choreRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryChoreRepository categoryChoreRepository;
    private static final int DEFAULT_PAGE_SIZE = 4;

    public List<ChoreResponse> getChoresByCategory(Long categoryId) {
        return choreRepository.findByCategoryChores_Category_Id(
                        categoryId,
                        PageRequest.of(0, DEFAULT_PAGE_SIZE)
                )
                .stream()
                .map(ChoreResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void createDummyData() {
        // 1. 카테고리 생성
        Category winter = Category
                .builder()
                .nameKo("겨울철 대청소")
                .id(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .description("겨울철에 진행하는 청소 모음")
                .isActive(true)
                .build();

        Category dailyTen = Category
                .builder()
                .id(2L)
                .nameKo("하루 10분 청소하기")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isActive(true)
                .build();

        // 2. 집안일 생성
        Chore vacuum = Chore.builder()
                .title("청소기 돌리기")
                .build();
        choreRepository.save(vacuum);

        Chore dishes = Chore.builder()
                .title("설거지 하기")
                .build();
        choreRepository.save(dishes);

        Chore laundry = Chore.builder()
                .title("빨래하기")
                .build();
        choreRepository.save(laundry);

        // 3. 카테고리-집안일 매핑
        categoryChoreRepository.save(
                CategoryChore.builder()
                        .category(dailyTen)
                        .chore(vacuum)
                        .build()
        );

        categoryChoreRepository.save(
                CategoryChore.builder()
                        .category(dailyTen)
                        .chore(dishes)
                        .build()
        );

        categoryChoreRepository.save(
                CategoryChore.builder()
                        .category(winter)
                        .chore(laundry)
                        .build()
        );

    }
}
