package com.zerobase.homemate.recommend;


import com.zerobase.homemate.recommend.dto.ChoreResponse;
import com.zerobase.homemate.recommend.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

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
        Long categoryId = 1L; // createDummyData에서 첫 번째로 저장된 카테고리 (예: 청소)

        // when
        List<ChoreResponse> chores = categoryService.getChoresByCategory(categoryId);

        // then
        assertThat(chores).isNotEmpty();
        chores.forEach(choreResponse -> System.out.println("조회된 집안일: " + choreResponse.getClass()));
    }
}
