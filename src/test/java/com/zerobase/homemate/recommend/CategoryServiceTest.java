package com.zerobase.homemate.recommend;


import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.recommend.dto.ChoreResponse;
import com.zerobase.homemate.recommend.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService.createDummyData();
    }

    @Test
    void 카테고리_조회_성공 (){
        // given
        Long categoryId = 4L; // createDummyData에서 첫 번째로 저장된 카테고리 (로컬 MySQL에서 저장된 카테고리 Id 값)

        // when
        List<ChoreResponse> chores = categoryService.getChoresByCategory(categoryId);

        // then
        assertThat(chores).isNotEmpty();
        chores.forEach(choreResponse -> System.out.println("조회된 집안일: " + choreResponse.getClass()));
    }

    @Test
    void 카테고리_조회_실패_존재하지_않는_카테고리() {
        // given
        Long invalidCategoryId = 999L;

        CustomException exception = assertThrows(CustomException.class,
                () -> categoryService.getChoresByCategory(invalidCategoryId));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_CATEGORY);
    }
}
