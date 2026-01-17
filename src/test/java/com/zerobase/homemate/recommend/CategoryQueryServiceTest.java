package com.zerobase.homemate.recommend;

import com.zerobase.homemate.entity.Categories;
import com.zerobase.homemate.entity.CategoryChore;
import com.zerobase.homemate.entity.enums.Category;
import com.zerobase.homemate.entity.enums.CategoryType;
import com.zerobase.homemate.entity.enums.RepeatType;
import com.zerobase.homemate.entity.enums.Season;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.recommend.dto.CategoryResponse;
import com.zerobase.homemate.recommend.dto.ClassifyChoreResponse;
import com.zerobase.homemate.recommend.service.CategoryQueryService;
import com.zerobase.homemate.repository.CategoriesRepository;
import com.zerobase.homemate.repository.CategoryChoreRepository;
import com.zerobase.homemate.repository.ChoreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.List;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CategoryQueryServiceTest {

    @InjectMocks
    private CategoryQueryService categoryQueryService;

    @Mock
    private CategoryChoreRepository categoryChoreRepository;

    @Mock
    private ChoreRepository choreRepository;

    @Mock
    private CategoriesRepository categoriesRepository;

    @Test
    void getFixedChores_nullCategory_throwsException() {
        assertThatThrownBy(() -> categoryQueryService.getFixedChores(null, 1L))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.CATEGORY_NOT_FOUND.getMessage());
    }

    @Test
    void getSeasonChores_nullSeason_throwsException() {
        assertThatThrownBy(() -> categoryQueryService.getSeasonChores(null, 1L))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.INVALID_SEASON.getMessage());
    }

    @Test
    void getMonthlyChores_categoryNotFound() {
        given(categoriesRepository.findById(1L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> categoryQueryService.getMonthlyChores(1L, null, any()))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.CATEGORY_NOT_FOUND.getMessage());
    }


    @Test
    void getFixedChores_success(){
        // given
        Category category = Category.TEN_MINUTES_CLEANING;

        CategoryChore testChore = CategoryChore.builder()
                .id(1L)
                .category(category)
                .repeatType(RepeatType.DAILY)
                .categoryType(CategoryType.FIXED)
                .repeatInterval(1)
                .title("설거지하기")
                .build();

        given(categoryChoreRepository.findActiveFixedByCategory(
                eq(category)
        )).willReturn(List.of(testChore));

        // when
        List<ClassifyChoreResponse> result =
                categoryQueryService.getFixedChores(category, any());

        // then
        assertThat(result).hasSize(1);
        verify(categoryChoreRepository).findActiveFixedByCategory(
                eq(category)
        );
    }

    @Test
    void getSeasonChores_success(){
        // given
        Season season = Season.WINTER;

        CategoryChore seasonTestChore = CategoryChore.builder()
                .id(2L)
                .season(season)
                .repeatType(RepeatType.WEEKLY)
                .repeatInterval(1)
                .title("창문 닦기")
                .categoryType(CategoryType.SEASON)
                .build();

        given(categoryChoreRepository.findActiveSeasonalBySeason(
                eq(season)
        )).willReturn(List.of(seasonTestChore));

        // when
        List<ClassifyChoreResponse> result =
                categoryQueryService.getSeasonChores(season, any());

        // then
        assertThat(result).hasSize(1);
        verify(categoryChoreRepository).findActiveSeasonalBySeason(
                eq(season)
        );
    }

    @Test
    void getMonthlyChores_success(){
        // given
        Categories categories = Categories.monthly("2026-01", "1월 추천", 1);

        CategoryChore testMonthChore = CategoryChore.builder()
                .id(3L)
                .categoryType(CategoryType.MONTHLY)
                .repeatType(RepeatType.MONTHLY)
                .repeatInterval(1)
                .categories(categories)
                .build();

        given(categoriesRepository.findById(categories.getId())).willReturn(Optional.of(categories));

        given(categoryChoreRepository.findActiveByCategoriesAndCategoryType(
                eq(categories),
                eq(CategoryType.MONTHLY)
        )).willReturn(List.of(testMonthChore));

        // when
        List<ClassifyChoreResponse> result =
                categoryQueryService.getMonthlyChores(categories.getId(), null, any());

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    void getMonthlyCategories_success() {
        Categories categories = Categories.monthly("2026-01", "1월 추천", 1);

        given(categoriesRepository.findActiveMonthlyByTargetMonth("2026-01"))
                .willReturn(List.of(categories));

        List<CategoryResponse> result =
                categoryQueryService.getMonthlyCategories("2026-01");

        assertThat(result).hasSize(1);
    }

    @Test
    void getFixedChores_duplicateFlag_isCorrect() {
        // given
        Long userId = 1L;
        Category category = Category.TEN_MINUTES_CLEANING;

        CategoryChore chore1 = CategoryChore.builder()
                .id(1L)
                .title("설거지하기")
                .repeatType(RepeatType.DAILY)
                .category(Category.TEN_MINUTES_CLEANING)
                .repeatType(RepeatType.DAILY)
                .repeatInterval(1)
                .categoryType(CategoryType.FIXED)
                .build();

        CategoryChore chore2 = CategoryChore.builder()
                .id(2L)
                .title("청소기 돌리기")
                .repeatType(RepeatType.DAILY)
                .categoryType(CategoryType.FIXED)
                .category(Category.TEN_MINUTES_CLEANING)
                .repeatType(RepeatType.DAILY)
                .repeatInterval(1)
                .build();

        given(categoryChoreRepository.findActiveFixedByCategory(eq(category)))
                .willReturn(List.of(chore1, chore2));

        // user는 이미 "설거지하기"를 등록함
        given(choreRepository.findActiveTitlesByUserId(userId))
                .willReturn(List.of("설거지하기"));

        // when
        List<ClassifyChoreResponse> result =
                categoryQueryService.getFixedChores(category, userId);

        // then
        assertThat(result).hasSize(2);

        assertThat(result)
                .anyMatch(r ->
                        r.title().equals("설거지하기") &&
                                Boolean.TRUE.equals(r.isDuplicate())
                );

        assertThat(result)
                .anyMatch(r ->
                        r.title().equals("청소기 돌리기") &&
                                Boolean.FALSE.equals(r.isDuplicate())
                );
    }



}
