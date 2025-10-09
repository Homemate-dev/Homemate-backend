package com.zerobase.homemate.recommend;

import com.zerobase.homemate.entity.Category;
import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.enums.RepeatType;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.recommend.dto.ChoreResponse;
import com.zerobase.homemate.recommend.service.CategoryService;
import com.zerobase.homemate.repository.CategoryRepository;
import com.zerobase.homemate.repository.ChoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

class CategoryServiceTest {

    @Mock
    private ChoreRepository choreRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("카테고리 ID로 집안일 조회 성공")
    void getChoresByCategory_success() {
        // given
        Long categoryId = 1L;
        Category category = Category.builder()
                .id(categoryId)
                .nameKo("청소")
                .isActive(true)
                .description("집안 청소 관련 카테고리")
                .build();

        Chore chore1 = Chore.builder()
                .id(1L)
                .title("거실 청소하기")
                .notificationYn(false)
                .space("거실")
                .repeatType(RepeatType.NONE)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now())
                .build();

        Chore chore2 = Chore.builder()
                .id(2L)
                .title("욕실 청소하기")
                .notificationYn(false)
                .space("욕실")
                .repeatType(RepeatType.NONE)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now())
                .build();

        given(categoryRepository.findById(categoryId))
                .willReturn(Optional.of(category));

        given(choreRepository.findByCategoryChores_Category_Id(eq(categoryId), any(PageRequest.class)))
                .willReturn(List.of(chore1, chore2));

        // when
        List<ChoreResponse> result = categoryService.getChoresByCategory(categoryId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).title()).isEqualTo("거실 청소하기");
        assertThat(result.get(1).title()).isEqualTo("욕실 청소하기");
        assertThat(result.get(0).frequency()).isEqualTo(RepeatType.NONE);
        verify(categoryRepository).findById(categoryId);
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 ID로 조회 시 예외 발생")
    void getChoresByCategory_categoryNotFound() {
        // given
        Long invalidId = 999L;
        given(categoryRepository.findById(invalidId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.getChoresByCategory(invalidId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.CATEGORY_NOT_FOUND.getMessage());
    }
}
