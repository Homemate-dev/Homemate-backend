package com.zerobase.homemate.recommend;

import com.zerobase.homemate.entity.Category;
import com.zerobase.homemate.entity.CategoryChore;
import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.enums.RepeatType;
import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.recommend.dto.ChoreResponse;
import com.zerobase.homemate.recommend.service.CategoryService;
import com.zerobase.homemate.repository.CategoryChoreRepository;
import com.zerobase.homemate.repository.CategoryRepository;
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
    private CategoryChoreRepository categoryChoreRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private Chore chore1, chore2;
    private CategoryChore cc1, cc2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        category = Category.builder()
                .id(1L)
                .nameKo("청소")
                .isActive(true)
                .build();

        chore1 = Chore.builder()
                .id(1L)
                .title("거실 청소")
                .notificationYn(false)
                .space(Space.LIVING_ROOM)
                .repeatType(RepeatType.DAILY)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now())
                .build();

        chore2 = Chore.builder()
                .id(2L)
                .title("욕실 청소")
                .notificationYn(false)
                .space(Space.BEDROOM)
                .repeatType(RepeatType.WEEKLY)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now())
                .build();

        cc1 = CategoryChore.builder().category(category).chore(chore1).build();
        cc2 = CategoryChore.builder().category(category).chore(chore2).build();
    }

    @Test
    @DisplayName("카테고리별 집안일 조회 성공")
    void getChoresByCategory_success() {
        // given
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
        given(categoryChoreRepository.findByCategory_Id(eq(1L), any(PageRequest.class)))
                .willReturn(List.of(cc1, cc2));

        // when
        List<ChoreResponse> result = categoryService.getChoresByCategory(1L);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).title()).isEqualTo("거실 청소");
        assertThat(result.get(1).title()).isEqualTo("욕실 청소");
        assertThat(result.get(0).frequency()).isEqualTo("매일");
        assertThat(result.get(1).frequency()).isEqualTo("매주");
        verify(categoryRepository).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 조회 시 예외 발생")
    void getChoresByCategory_categoryNotFound() {
        // given
        given(categoryRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.getChoresByCategory(999L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.CATEGORY_NOT_FOUND.getMessage());
    }
}
