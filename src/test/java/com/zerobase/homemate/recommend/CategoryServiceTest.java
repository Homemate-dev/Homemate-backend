package com.zerobase.homemate.recommend;


import com.zerobase.homemate.entity.CategoryChore;
import com.zerobase.homemate.entity.enums.Category;
import com.zerobase.homemate.entity.enums.RepeatType;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.recommend.service.CategoryService;
import com.zerobase.homemate.repository.CategoryChoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Pageable;


import java.util.List;

import static com.zerobase.homemate.entity.enums.RepeatType.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CategoryServiceTest {

    @Mock
    private CategoryChoreRepository categoryChoreRepository;

    @InjectMocks
    CategoryService categoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("카테고리별 집안일을 조회 시, 집안일들은 주기 순으로 정렬값을 반환한다.")
    @Test
    void getChoresByCategory_success(){
        // given
        Category category = Category.WINTER;

        CategoryChore cc1 =  CategoryChore.builder()
                .category(Category.WINTER)
                .title("가습기 세척하기")
                .repeatType(RepeatType.MONTHLY)
                .repeatInterval(3)
                .build();

        CategoryChore cc2 = CategoryChore.builder()
                .category(Category.WINTER)
                .title("베개커버, 이불커버 세탁하기")
                .repeatType(RepeatType.WEEKLY)
                .repeatInterval(2)
                .build();

        CategoryChore cc3 = CategoryChore.builder()
                .category(Category.WINTER)
                .title("옷장 정리하기")
                .repeatType(DAILY)
                .repeatInterval(1)
                .build();

        CategoryChore cc4 = CategoryChore.builder()
                .category(Category.WINTER)
                .title("보일러 점검하기")
                .repeatType(RepeatType.NONE)
                .repeatInterval(0)
                .build();

        categoryChoreRepository.save(cc1);
        categoryChoreRepository.save(cc2);
        categoryChoreRepository.save(cc3);
        categoryChoreRepository.save(cc4);

        when(categoryChoreRepository.findByCategory(eq(category), any(Pageable.class)))
                .thenReturn(List.of(cc1, cc2, cc3, cc4));


        // when
        var result = categoryService.getChoresByCategory(category);

        // then
        assertThat(result).hasSize(4);
        assertThat(result.get(0).frequency()).isEqualTo("매일");      // cc3
        assertThat(result.get(1).frequency()).isEqualTo("2주");      // cc2
        assertThat(result.get(2).frequency()).isEqualTo("3개월");      // cc1
        assertThat(result.get(3).frequency()).isEqualTo("한번"); // cc4

        verify(categoryChoreRepository, times(1))
                .findByCategory (eq(category), any(Pageable.class));
    }

    @Test
    @DisplayName("카테고리가 null이면 CATEGORY_NOT_FOUND 예외 발생")
    void getChoresByCategory_nullCategory_fail() {
        assertThatThrownBy(() -> categoryService.getChoresByCategory(null))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.CATEGORY_NOT_FOUND.getMessage());
    }


}
